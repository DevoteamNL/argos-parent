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
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestServiceAccountKeyPair;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountKeyPairMapperTest {

    private AccountKeyPairMapper converter;
    private ObjectMapper mapper;
    private String keyPairJson;
    private String base64EncodedPublicKey;

    @Mock
    private PasswordEncoder passwordEncoder;


    @BeforeEach
    void setUp() throws IOException {
        converter = Mappers.getMapper(AccountKeyPairMapper.class);
        ReflectionTestUtils.setField(converter, "passwordEncoder", passwordEncoder);
        mapper = new ObjectMapper();
        keyPairJson = IOUtils.toString(getClass().getResourceAsStream("/keypair.json"), UTF_8);
        base64EncodedPublicKey = IOUtils.toString(getClass().getResourceAsStream("/testkey.pub"), UTF_8);
    }

    @Test
    void serviceAccountKeyPair() throws IOException, JSONException {
        when(passwordEncoder.encode("hashedKeyPassphrase")).thenReturn("encodedHashedKeyPassphrase");
        String saPairJson = IOUtils.toString(getClass().getResourceAsStream("/sa-keypair.json"), UTF_8);
        RestServiceAccountKeyPair restSaKeyPair = mapper.readValue(saPairJson, RestServiceAccountKeyPair.class);
        ServiceAccountKeyPair saKeyPair = converter.convertFromRestKeyPair(restSaKeyPair);
        assertThat(saKeyPair.getEncryptedHashedKeyPassphrase(), is("encodedHashedKeyPassphrase"));
        
        RestServiceAccountKeyPair restServiceAccountKeyPair = converter.convertToRestKeyPair(saKeyPair);
        assertThat(restServiceAccountKeyPair.getHashedKeyPassphrase(), nullValue());
        restServiceAccountKeyPair.setHashedKeyPassphrase("hashedKeyPassphrase");
        JSONAssert.assertEquals(saPairJson, mapper.writeValueAsString(restServiceAccountKeyPair), true);

    }

    @Test
    void convertFromRestKeyPair() throws JsonProcessingException, JSONException {
        KeyPair keyPair = converter.convertFromRestKeyPair(mapper.readValue(keyPairJson, RestKeyPair.class));
        JSONAssert.assertEquals(keyPairJson, mapper.writeValueAsString(converter.convertToRestKeyPair(keyPair)), true);
    }
}
