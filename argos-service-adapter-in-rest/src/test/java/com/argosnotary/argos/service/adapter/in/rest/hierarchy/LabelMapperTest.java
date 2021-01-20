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
package com.argosnotary.argos.service.adapter.in.rest.hierarchy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.domain.hierarchy.Label;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLabel;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;

class LabelMapperTest {

    private LabelMapper converter;
    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(LabelMapper.class);
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(LabelMapperTest.class.getResourceAsStream("/label.json"), StandardCharsets.UTF_8);
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException, JSONException {
        Label label = converter.convertFromRestLabel(mapper.readValue(linkJson, RestLabel.class));
        RestLabel restLabel = converter.convertToRestLabel(label);
        assertThat(restLabel.getId(), is(label.getLabelId()));
        assertThat(restLabel.getId(), hasLength(36));
        restLabel.setId("labelId");
        JSONAssert.assertEquals(linkJson, mapper.writeValueAsString(restLabel), true);
    }
}
