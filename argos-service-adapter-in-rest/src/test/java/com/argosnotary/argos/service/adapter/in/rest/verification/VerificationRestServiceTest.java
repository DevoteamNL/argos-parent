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
package com.argosnotary.argos.service.adapter.in.rest.verification;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.release.ReleaseRepository;
import com.argosnotary.argos.service.domain.verification.VerificationProvider;
import com.argosnotary.argos.service.domain.verification.VerificationRunResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationRestServiceTest {

    @Mock
    private VerificationProvider verificationProvider;

    @Mock
    private LayoutMetaBlockRepository layoutMetaBlockRepository;

    @Mock
    private ArtifactMapper artifactMapper;

    @Mock
    private VerificationResultMapper verificationResultMapper;

    @Mock
    private RestVerifyCommand restVerifyCommand;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private Artifact artifact;

    @Mock
    private RestArtifact restArtifact;

    @Mock
    private LayoutMetaBlock layoutMetaBlockMetaBlock;

    private VerificationRestService verificationRestService;


    @BeforeEach
    void setup() {
        verificationRestService = new VerificationRestService(
                verificationProvider,
                layoutMetaBlockRepository,
                releaseRepository,
                artifactMapper,
                verificationResultMapper);

    }

    @Test
    void performVerificationShouldReturnOk() {
        VerificationRunResult runResult = VerificationRunResult.okay();
        RestVerificationResult restVerificationResult = new RestVerificationResult();
        restVerificationResult.setRunIsValid(true);
        when(layoutMetaBlockRepository.findBySupplyChainId(eq("supplyChainId")))
                .thenReturn(Optional.of(layoutMetaBlockMetaBlock));
        when(restVerifyCommand.getExpectedProducts()).thenReturn(singletonList(restArtifact));
        when(artifactMapper.mapToArtifacts(any())).thenReturn(singletonList(artifact));
        when(verificationProvider.verifyRun(any(), any())).thenReturn(runResult);
        when(verificationResultMapper.mapToRestVerificationResult(eq(runResult))).thenReturn(restVerificationResult);
        ResponseEntity<RestVerificationResult> result = verificationRestService.performVerification("supplyChainId", restVerifyCommand);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getRunIsValid(), is(true));
    }

    @Test
    void performVerificationWithNoLayoutShouldReturnError() {
        when(layoutMetaBlockRepository.findBySupplyChainId(eq("supplyChainId")))
                .thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> verificationRestService.performVerification("supplyChainId", restVerifyCommand));
        assertThat(error.getStatus().value(), is(400));
    }


    @Test
    void getVerification() {
        when(releaseRepository.artifactsAreReleased(any(), any())).thenReturn(true);
        ResponseEntity<RestVerificationResult> result = verificationRestService.getVerification(List.of("hash"), List.of("path"));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody().getRunIsValid(), is(true));
    }
}