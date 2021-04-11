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
package com.argosnotary.argos.service.adapter.in.rest.verification;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

class ArtifactMapperTest {

    public static final String HASH = "hash";
    public static final String URI = "uri";
    private ArtifactMapper artifactMapper;

    @BeforeEach
    public void setup() {
        artifactMapper = Mappers.getMapper(ArtifactMapper.class);
    }

    @Test
    void mapToArtifactsShould_Return_Artifacts() {
        RestArtifact restArtifact = new RestArtifact();
        restArtifact.setHash(HASH);
        restArtifact.setUri(URI);
        Set<Artifact> artifacts = artifactMapper.mapToArtifacts(singletonList(restArtifact));
        assertThat(artifacts, hasSize(1));
        assertThat(artifacts.iterator().next().getHash(), is(HASH));
        assertThat(artifacts.iterator().next().getUri(), is(URI));
    }

}