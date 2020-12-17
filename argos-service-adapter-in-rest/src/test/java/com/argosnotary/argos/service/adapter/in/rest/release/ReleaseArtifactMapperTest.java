/*
 * Copyright (C) 2020 Argos Notary
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
package com.argosnotary.argos.service.adapter.in.rest.release;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class ReleaseArtifactMapperTest {
    protected static final String HASH = "hash";
    protected static final String TARGET = "/target/";
    private ReleaseArtifactMapper releaseArtifactMapper;

    @BeforeEach
    void setUp() {
        releaseArtifactMapper = Mappers.getMapper(ReleaseArtifactMapper.class);

    }

    @Test
    void mapToArtifacts() {
        RestArtifact restArtifact = new RestArtifact().hash(HASH).uri(TARGET);
        List<Set<Artifact>> artifacts = releaseArtifactMapper.mapToArtifacts(Collections
                .singletonList(Collections.
                        singletonList(restArtifact)));

        assertThat(artifacts, hasSize(1));
        assertThat(artifacts.iterator().next(), hasSize(1));
        Artifact artifact = artifacts.iterator().next().iterator().next();
        assertThat(artifact.getHash(), is(HASH));
        assertThat(artifact.getUri(), is(TARGET));
    }

    @Test
    void mapToSetArtifacts() {
        RestArtifact restArtifact = new RestArtifact().hash(HASH).uri(TARGET);
        Set<Artifact> artifacts = releaseArtifactMapper.mapToSetArtifacts(Collections.singletonList(restArtifact));
        Artifact artifact = artifacts.iterator().next();
        assertThat(artifact.getHash(), is(HASH));
        assertThat(artifact.getUri(), is(TARGET));
    }
}