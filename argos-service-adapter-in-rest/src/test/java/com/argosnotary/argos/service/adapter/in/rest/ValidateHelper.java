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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ValidateHelper {

    @Builder
    @EqualsAndHashCode
    @ToString
    public static class ValidationError {
        private final String path;
        private final String message;
    }

    public static <T> List<ValidationError> validate(T object) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return validator.validate(object).stream()
                .sorted(Comparator.comparing((ConstraintViolation<T> cv) -> cv.getPropertyPath().toString())
                        .thenComparing(ConstraintViolation::getMessage))
                .map(cv -> ValidationError.builder().message(cv.getMessage()).path(cv.getPropertyPath().toString()).build())
                .collect(Collectors.toList());
    }

    public static ValidationError[] expectedErrors(String... errors) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (int i = 0; i < errors.length; i = i + 2) {
            validationErrors.add(ValidationError.builder().path(errors[i]).message(errors[i + 1]).build());
        }
        return validationErrors.toArray(new ValidationError[0]);
    }
}
