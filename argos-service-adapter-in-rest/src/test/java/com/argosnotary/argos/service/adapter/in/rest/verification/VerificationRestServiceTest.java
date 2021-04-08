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
import java.util.Set;

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
        when(artifactMapper.mapToArtifacts(any())).thenReturn(Set.of(artifact));
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