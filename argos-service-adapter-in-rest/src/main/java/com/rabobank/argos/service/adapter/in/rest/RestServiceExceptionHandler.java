/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationError;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestValidationMessage;
import com.rabobank.argos.service.adapter.in.rest.layout.LayoutValidationException;
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
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RestServiceExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<RestError> handleConstraintViolationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance).map(error -> ((FieldError) error).getField() + ":" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createMessage(message));
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<RestError> handleConstraintViolationException(
            ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(error -> error.getPropertyPath() + ":" + error.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createMessage(message));
    }

    @ExceptionHandler(value = {JsonMappingException.class})
    public ResponseEntity<RestError> handleJsonMappingException() {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createMessage("invalid json"));
    }

    @ExceptionHandler(value = {LayoutValidationException.class})
    public ResponseEntity<RestValidationError> handleLayoutValidationException(LayoutValidationException ex) {
        return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createValidationError(ex));
    }

    private RestValidationError createValidationError(LayoutValidationException ex) {
        RestValidationError restValidationError = new RestValidationError();
        List<RestValidationMessage> messages = new ArrayList<>();
        ex.getValidationMessages()
                .entrySet()
                .forEach(set ->
                        set.getValue()
                                .forEach(message -> {
                                    RestValidationMessage validationMessage = new RestValidationMessage();
                                    validationMessage.setField(set.getKey());
                                    validationMessage.setMessage(message);
                                    messages.add(validationMessage);
                                }));
        restValidationError.setMessages(messages);
        return restValidationError;
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<RestError> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus()).contentType(APPLICATION_JSON).body(createMessage(ex.getReason()));
    }

    @ExceptionHandler(value = {ArgosError.class})
    public ResponseEntity<RestError> handleArgosError(ArgosError ex) {
        if (ex.getLevel() == ArgosError.Level.WARNING) {
            log.debug("{}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().contentType(APPLICATION_JSON).body(createMessage(ex.getMessage()));
        } else {
            log.error("{}", ex.getMessage(), ex);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON).body(createMessage(ex.getMessage()));
        }
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<RestError> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(FORBIDDEN).contentType(APPLICATION_JSON).body(createMessage(ex.getMessage()));
    }

    private RestError createMessage(String message) {
        return new RestError().message(message);
    }


}
