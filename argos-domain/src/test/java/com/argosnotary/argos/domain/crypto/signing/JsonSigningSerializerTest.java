/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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
package com.argosnotary.argos.domain.crypto.signing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
                .runId("runId")
                .layoutSegmentName("layoutSegmentName")
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
                        .destinationSegmentName("destinationSegmentName")
                        .destinationType(ArtifactType.PRODUCTS)
                        .destinationStepName("destinationStepName")
                        .pattern("MatchFiler").build()))
                .layoutSegments(Arrays.asList(LayoutSegment.builder().name("segment2").steps(new ArrayList<>()).build(), 
                        LayoutSegment.builder().name("segment1")
                        .steps(Arrays.asList(
                                Step.builder()
                                        .name("stepb")
                                        .requiredNumberOfLinks(1)
                                        .expectedMaterials(Arrays.asList(
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
                                                        .destinationSegmentName("segment1")
                                                        .destinationType(ArtifactType.MATERIALS)
                                                        .build(),
                                                new Rule(RuleType.DELETE, "DeleteRule")
                                        ))
                                        .build()
                        )).build()))
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
