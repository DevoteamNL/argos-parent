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
package com.argosnotary.argos.domain.release;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ReleaseDossierMetaDataTest {

    protected static final String HASH = "71ed24f24e838b18a4bc53aac2638155692b43289ca9778c37139859fc6e619d";
    protected static final List<String> ARTIFACT_LIST;

    static {
        ARTIFACT_LIST = new ArrayList<>();
        ARTIFACT_LIST.add("string2");
        ARTIFACT_LIST.add("string");
    }

    @Test
    void createHashFromArtifactList() {
        String result = ReleaseDossierMetaData.createHashFromArtifactList(ARTIFACT_LIST);
        assertThat(result, is(HASH));
    }
    
    @Test
    void equalsTest() {
        List<List<String>> releaseArtifacts = new ArrayList<>();
        releaseArtifacts.add(ARTIFACT_LIST);
        ReleaseDossierMetaData dossier = ReleaseDossierMetaData.builder().documentId("documentId").releaseArtifacts(releaseArtifacts).build();
        ReleaseDossierMetaData dossier2 = ReleaseDossierMetaData.builder().documentId("documentId").releaseArtifacts(releaseArtifacts).build();
        assertThat(dossier, is(dossier2));
    }
    
    @Test
    void buildTest() {
        OffsetDateTime time = OffsetDateTime.now();
        List<List<String>> releaseArtifacts = new ArrayList<>();
        releaseArtifacts.add(ARTIFACT_LIST);
        ReleaseDossierMetaData dossier = ReleaseDossierMetaData.builder()
                .documentId("documentId")
                .releaseArtifacts(releaseArtifacts)
                .releaseDate(time)
                .supplyChainPath("foo.bar:sc")
                .build();
        assertThat(dossier.getDocumentId(), is("documentId"));
        assertThat(dossier.getReleaseArtifacts(), is(releaseArtifacts));
        assertThat(dossier.getReleaseDate(), is(time));
        assertThat(dossier.getSupplyChainPath(), is("foo.bar:sc"));
    }
    
    @Test
    void settersTest() {
        OffsetDateTime time = OffsetDateTime.now();
        List<List<String>> releaseArtifacts = new ArrayList<>();
        releaseArtifacts.add(ARTIFACT_LIST);
        ReleaseDossierMetaData dossier = ReleaseDossierMetaData.builder().build();
        dossier.setDocumentId("documentId");
        dossier.setReleaseArtifacts(releaseArtifacts);
        dossier.setReleaseDate(time);
        dossier.setSupplyChainPath("foo.bar:sc");
        assertThat(dossier.getDocumentId(), is("documentId"));
        assertThat(dossier.getReleaseArtifacts(), is(releaseArtifacts));
        assertThat(dossier.getReleaseDate(), is(time));
        assertThat(dossier.getSupplyChainPath(), is("foo.bar:sc"));
    }
}