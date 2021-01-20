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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JacksonOffsetDateTimeMapperTest {


    JacksonOffsetDateTimeMapper jacksonOffsetDateTimeMapper;

    @BeforeEach
    void setUp() {
        jacksonOffsetDateTimeMapper = new JacksonOffsetDateTimeMapper();
    }

    @SneakyThrows
    @Test
    void objectMapper() {
        ObjectMapper objectMapper = jacksonOffsetDateTimeMapper.objectMapper();
        String dateTime = "2020-07-30T18:35:24Z";
        DateTimeObject dateTimeObject = new DateTimeObject();
        dateTimeObject.setOffsetDateTime(OffsetDateTime.parse(dateTime));
        String jsonString = objectMapper.writeValueAsString(dateTimeObject);
        assertThat(jsonString, is("{\"offsetDateTime\":\"2020-07-30T18:35:24Z\"}"));
    }

    @Data
    static class DateTimeObject {
        private OffsetDateTime offsetDateTime;
    }
}