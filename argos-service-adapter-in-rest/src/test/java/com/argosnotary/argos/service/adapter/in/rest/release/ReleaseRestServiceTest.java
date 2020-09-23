/*
 * Copyright (C) 2020 Argos Notary Cooperative
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