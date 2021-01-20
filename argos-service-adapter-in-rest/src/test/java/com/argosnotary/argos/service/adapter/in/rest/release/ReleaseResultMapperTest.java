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