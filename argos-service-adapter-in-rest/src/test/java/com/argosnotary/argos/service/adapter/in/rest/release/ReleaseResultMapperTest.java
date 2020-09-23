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

import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReleaseResultMapperTest {
    protected static final String DOCUMENT_ID = "id";
    protected static final String PATH = "path";
    protected static final String HASH = "hash";
    protected static final String RELEASE_DATE_TIME = "2020-07-30T18:35:24.00Z";
    private ReleaseResultMapper releaseResultMapper;

    @BeforeEach
    void setUp() {
        releaseResultMapper = Mappers.getMapper(ReleaseResultMapper.class);

    }

    @Test
    void maptoRestReleaseResult() {
        ReleaseResult releaseResult = ReleaseResult
                .builder()
                .releaseIsValid(true)
                .releaseDossierMetaData(ReleaseDossierMetaData
                        .builder()
                        .releaseDate(OffsetDateTime.parse(RELEASE_DATE_TIME))
                        .documentId(DOCUMENT_ID)
                        .releaseArtifacts(Collections
                                .singletonList(Collections
                                        .singletonList(HASH)))
                        .supplyChainPath(PATH)
                        .build())
                .build();

        RestReleaseResult restReleaseResult = releaseResultMapper.maptoRestReleaseResult(releaseResult);
        assertThat(restReleaseResult.getReleaseIsValid(), is(true));
        assertThat(restReleaseResult.getReleaseDossierMetaData().getDocumentId(), is(DOCUMENT_ID));
        assertThat(restReleaseResult.getReleaseDossierMetaData().getReleaseDate().toString(), is("2020-07-30T18:35:24Z"));
        assertThat(restReleaseResult.getReleaseDossierMetaData().getSupplyChainPath(), is(PATH));
        assertThat(restReleaseResult.getReleaseDossierMetaData().getReleaseArtifacts().iterator().next().iterator().next(), is(HASH));
    }

}