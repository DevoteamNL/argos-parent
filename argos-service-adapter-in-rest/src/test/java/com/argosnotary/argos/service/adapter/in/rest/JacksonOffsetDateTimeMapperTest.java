/*
 * Copyright (C) 2020 Argos Notary Cooperative
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