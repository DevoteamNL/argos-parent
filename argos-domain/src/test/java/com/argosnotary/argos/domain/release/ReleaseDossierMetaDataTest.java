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