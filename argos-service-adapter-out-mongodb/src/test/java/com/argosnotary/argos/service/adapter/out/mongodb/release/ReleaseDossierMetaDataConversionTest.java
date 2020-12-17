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