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