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
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseResult;
import com.argosnotary.argos.service.domain.release.ReleaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReleaseRestServiceTest {
    @Mock
    private ReleaseService releaseService;
    @Mock
    private ReleaseArtifactMapper artifactMapper;
    @Mock
    private ReleaseResultMapper releaseResultMapper;

    @Mock
    private RestReleaseArtifacts restReleaseArtifacts;

    @Mock
    private RestArtifact restArtifact;

    @Mock
    private ReleaseResult releaseResult;

    @Mock
    private RestReleaseResult restReleaseResult;

    @Mock
    private Artifact artifact;


    ReleaseRestService releaseRestService;


    @BeforeEach
    void setUp() {
        releaseRestService = new ReleaseRestService(releaseService, artifactMapper, releaseResultMapper);
    }

    @Test
    void createReleaseShouldReturn200() {
        when(releaseService.createRelease(any(), any())).thenReturn(releaseResult);
        when(artifactMapper.mapToArtifacts(singletonList(singletonList(restArtifact))))
                .thenReturn(singletonList(Set.of(artifact)));
        when(releaseResultMapper.maptoRestReleaseResult(releaseResult)).thenReturn(restReleaseResult);
        when(restReleaseArtifacts.getReleaseArtifacts()).thenReturn(singletonList(singletonList(restArtifact)));
        ResponseEntity<RestReleaseResult> result = releaseRestService.createRelease("id", restReleaseArtifacts);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }
}