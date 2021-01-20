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
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {LayoutMetaBlockMapperImpl.class, RuleMapperImpl.class, MatchRuleMapperImpl.class, StepMapperImpl.class})
class LayoutMetaBlockMapperTest {

    @Autowired
    private LayoutMetaBlockMapper converter;
    private ObjectMapper mapper;
    private String layoutJson;

    @BeforeEach
    void setUp() throws IOException {

        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        layoutJson = IOUtils.toString(getClass().getResourceAsStream("/layout.json"), UTF_8);
    }

    @Test
    void convertFromRestLayoutMetaBlock() throws JsonProcessingException, JSONException {
        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(mapper.readValue(layoutJson, RestLayoutMetaBlock.class));
        RestLayoutMetaBlock restLayoutMetaBlock = converter.convertToRestLayoutMetaBlock(layoutMetaBlock);
        JSONAssert.assertEquals(layoutJson, mapper.writeValueAsString(restLayoutMetaBlock), true);
    }

}
