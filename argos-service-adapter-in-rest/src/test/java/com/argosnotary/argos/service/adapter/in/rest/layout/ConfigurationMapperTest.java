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
package com.argosnotary.argos.service.adapter.in.rest.layout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

class ConfigurationMapperTest {

    private ConfigurationMapper approvalConfigMapper;
    private ObjectMapper mapper;
    private String approvalConfigJson;


    @BeforeEach
    void setup() throws IOException {
        approvalConfigMapper = Mappers.getMapper(ConfigurationMapper.class);
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        approvalConfigJson = IOUtils.toString(getClass().getResourceAsStream("/approval-config.json"), UTF_8);

    }

    @Test
    void shouldConvertCorrectLy() throws JsonProcessingException, JSONException {
        ApprovalConfiguration approvalConfiguration = approvalConfigMapper.convertFromRestApprovalConfiguration(mapper.readValue(approvalConfigJson, RestApprovalConfiguration.class));
        RestApprovalConfiguration restApprovalConfiguration = approvalConfigMapper.convertToRestApprovalConfiguration(approvalConfiguration);
        JSONAssert.assertEquals(approvalConfigJson, mapper.writeValueAsString(restApprovalConfiguration), new CustomComparator(
                JSONCompareMode.STRICT)
        );
    }
}