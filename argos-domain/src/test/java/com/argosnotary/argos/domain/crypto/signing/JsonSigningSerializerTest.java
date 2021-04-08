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
package com.argosnotary.argos.domain.crypto.signing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Base64;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class JsonSigningSerializerTest {

    private final static String PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEjdjAZjwvCrYGEv/zKVAhSItOV91OpPGmMPNCR3Dr0oryke0PhRO6HCbb+kS5NOJUEaGHbFeJUujpn/zQQIVlkQ==";

    @Test
    void serializeLink() throws IOException {
        String serialized = new JsonSigningSerializer().serialize(Link.builder()
                .stepName("stepName")
                .materials(Arrays.asList(
                        Artifact.builder().uri("zbc.jar").hash("hash1").build(),
                        Artifact.builder().uri("abc.jar").hash("hash2").build()))
                .products(Arrays.asList(
                        Artifact.builder().uri("_bc.jar").hash("hash3").build(),
                        Artifact.builder().uri("_abc.jar").hash("hash4").build()))
                .build());
        assertThat(serialized, is(getExpectedJson("/expectedLinkSigning.json")));
    }

    @Test
    void serializeLayout() throws IOException, GeneralSecurityException {
    	
    	Layout layout = Layout.builder()
                .keys(Arrays.asList(new PublicKey("keyId", Base64.getDecoder().decode(PUBLIC_KEY))))
                .expectedEndProducts(singletonList(MatchRule.builder()
                        .destinationType(ArtifactType.PRODUCTS)
                        .destinationStepName("destinationStepName")
                        .pattern("MatchFiler").build()))
                .steps(Arrays.asList(
                        Step.builder()
                            .name("stepb")
                            .requiredNumberOfLinks(1)
                            .expectedMaterials(
                                    Arrays.asList(
                                            new Rule(RuleType.ALLOW, "AllowRule"),
                                            new Rule(RuleType.REQUIRE, "RequireRule")
                                    ))
                            .expectedProducts(Arrays.asList(
                                    new Rule(RuleType.CREATE, "CreateRule"),
                                    new Rule(RuleType.MODIFY, "ModifyRule")
                                    ))
                            .build(),
                        Step.builder()
                            .name("stepa")
                            .authorizedKeyIds(Arrays.asList("step a key 2", "step a key 1"))
                            .requiredNumberOfLinks(23)
                            .expectedProducts(Arrays.asList(
                                    new Rule(RuleType.DISALLOW, "DisAllowRule"),
                                    MatchRule.builder().pattern("MatchRule")
                                        .destinationPathPrefix("destinationPathPrefix")
                                        .sourcePathPrefix("sourcePathPrefix")
                                        .destinationStepName("destinationStepName")
                                        .destinationType(ArtifactType.MATERIALS)
                                        .build(),
                                    new Rule(RuleType.DELETE, "DeleteRule")
                                    ))
                        .build()
                        ))
                .authorizedKeyIds(Arrays.asList("key2", "key1"))
                .build();
        String serialized = new JsonSigningSerializer().serialize(layout);
        assertThat(serialized, is(getExpectedJson("/expectedLayoutSigning.json")));
    }

    private String getExpectedJson(String name) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(getClass().getResourceAsStream(name), JsonNode.class);
        return jsonNode.toString();
    }
}
