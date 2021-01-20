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
package com.argosnotary.argos.service.adapter.out.mongodb.release;

import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

class ReleaseDossierMetaDataConversionTest {

    protected static final List<String> ARTIFACT_LIST = List.of("string", "string2");
    protected static final Date DATE = new Date(1596716015548L);

    @Test
    void convertToWithDocumentId() {
        DateToOffsetTimeConverter converter = new DateToOffsetTimeConverter();
        ReleaseDossierMetaData expected = ReleaseDossierMetaData.builder()
                .releaseArtifacts(List.of(ARTIFACT_LIST))
                .documentId("54651022bffebc03098b4567")
                .supplyChainPath("foo.bar")
                .releaseDate(converter.convert(DATE))
                .build();
        ReleaseDossierMetaDataToDocumentConverter dossierConverter = new ReleaseDossierMetaDataToDocumentConverter();
        DocumentToReleaseDossierMetaDataConverter backConverter = new DocumentToReleaseDossierMetaDataConverter();
        ReleaseDossierMetaData actual = backConverter.convert(dossierConverter.convert(expected));
        assertEquals(expected, actual);
    }
    
    @Test
    void convertToWithoutDocumentId() {
        DateToOffsetTimeConverter converter = new DateToOffsetTimeConverter();
        ReleaseDossierMetaData expected = ReleaseDossierMetaData.builder()
                .releaseArtifacts(List.of(ARTIFACT_LIST))
                .supplyChainPath("foo.bar")
                .releaseDate(converter.convert(DATE))
                .build();
        ReleaseDossierMetaDataToDocumentConverter dossierConverter = new ReleaseDossierMetaDataToDocumentConverter();
        DocumentToReleaseDossierMetaDataConverter backConverter = new DocumentToReleaseDossierMetaDataConverter();
        ReleaseDossierMetaData actual = backConverter.convert(dossierConverter.convert(expected));
        assertEquals(expected, actual);
    }

}