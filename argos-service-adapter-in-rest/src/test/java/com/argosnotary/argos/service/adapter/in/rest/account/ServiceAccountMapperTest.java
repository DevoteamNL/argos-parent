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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestServiceAccount;
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

class ServiceAccountMapperTest {

    private ServiceAccountMapper converter;
    private ObjectMapper mapper;
    private String linkJson;

    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(ServiceAccountMapper.class);
        mapper = new ObjectMapper();
        linkJson = IOUtils.toString(ServiceAccountMapperTest.class.getResourceAsStream("/service-account.json"), StandardCharsets.UTF_8);
    }

    @Test
    void convertFromRestLinkMetaBlock() throws JsonProcessingException, JSONException {
        ServiceAccount serviceAccount = converter.convertFromRestServiceAccount(mapper.readValue(linkJson, RestServiceAccount.class));
        RestServiceAccount restServiceAccount = converter.convertToRestServiceAccount(serviceAccount);
        assertThat(restServiceAccount.getId(), is(serviceAccount.getAccountId()));
        assertThat(restServiceAccount.getId(), hasLength(36));
        restServiceAccount.setId("accountId");
        JSONAssert.assertEquals(linkJson, mapper.writeValueAsString(restServiceAccount), true);
    }
}
