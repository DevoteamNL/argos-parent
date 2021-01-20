/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.service.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestError;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationError;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage;
import com.argosnotary.argos.service.adapter.in.rest.layout.LayoutValidationException;
import com.argosnotary.argos.service.domain.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestServiceExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<RestValidationError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<RestValidationMessage> validationMessages = ex.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(error -> new RestValidationMessage()
                        .field(((FieldError) error).getField())
                        .message(error.getDefaultMessage())
                        .type(DATA_INPUT)
                )
                .collect(Collectors.toList());

        sortValidationMessages(validationMessages);

        RestValidationError restValidationError = new RestValidationError().messages(validationMessages);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(restValidationError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestValidationError> handleConstraintViolationException(
            ConstraintViolationException ex) {
        List<RestValidationMessage> validationMessages = ex.getConstraintViolations()
                .stream()
                .map(error -> new RestValidationMessage()
                        .field(error.getPropertyPath().toString())
                        .message(error.getMessage())
                        .type(DATA_INPUT)
                )
                .collect(Collectors.toList());
        sortValidationMessages(validationMessages);
        RestValidationError restValidationError = new RestValidationError().messages(validationMessages);
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(restValidationError);
    }

    @ExceptionHandler(value = {JsonMappingException.class})
    public ResponseEntity<RestValidationError> handleJsonMappingException(JsonMappingException ex) {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError("invalid json"));
    }

    @ExceptionHandler(value = {LayoutValidationException.class})
    public ResponseEntity<RestValidationError> handleLayoutValidationException(LayoutValidationException ex) {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex));
    }


    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity handleResponseStatusException(ResponseStatusException ex) {
        if (BAD_REQUEST == ex.getStatus()) {
            return ResponseEntity.status(ex.getStatus()).contentType(APPLICATION_JSON).body(createValidationError(ex.getReason()));
        } else {
            return ResponseEntity.status(ex.getStatus()).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getReason()));
        }
    }

    @ExceptionHandler(value = {ArgosError.class})
    public ResponseEntity handleArgosError(ArgosError ex) {
        if (ex.getLevel() == ArgosError.Level.WARNING) {
            log.debug("{}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex.getMessage()));
        } else {
            log.error("{}", ex.getMessage(), ex);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
        }
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<RestError> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(FORBIDDEN).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<RestError> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND).contentType(APPLICATION_JSON).body(createRestErrorMessage(ex.getMessage()));
    }

    private RestValidationError createValidationError(LayoutValidationException ex) {
        RestValidationError restValidationError = new RestValidationError();
        List<RestValidationMessage> validationMessages = new ArrayList<>(ex.getValidationMessages());
        sortValidationMessages(validationMessages);
        validationMessages.forEach(message -> log.error("Rest Validation Error: [{}]", message));
        restValidationError.setMessages(validationMessages);
        return restValidationError;
    }

    private void sortValidationMessages(List<RestValidationMessage> validationMessages) {
        validationMessages.sort(Comparator
                .comparing(RestValidationMessage::getField)
                .thenComparing(RestValidationMessage::getMessage));
    }

    private RestValidationError createValidationError(String reason) {
        log.error("Rest Validation Error: [{}]", reason);
        return new RestValidationError()
                .messages(singletonList(new RestValidationMessage()
                        .message(reason)
                        .type(DATA_INPUT)));
    }

    private RestError createRestErrorMessage(String message) {
        log.error("Rest Error: [{}]", message);
        return new RestError().message(message);
    }

}
