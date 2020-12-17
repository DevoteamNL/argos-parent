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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData.ReleaseDossierMetaDataBuilder;

import static com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.*;

public class DocumentToReleaseDossierMetaDataConverter implements Converter<Document, ReleaseDossierMetaData>{

    @Override
    public ReleaseDossierMetaData convert(Document source) {
        List<Document> releaseArtifactsList = source
                .getList(RELEASE_ARTIFACTS_FIELD, Document.class,
                        Collections.emptyList());
        ReleaseDossierMetaDataBuilder builder =  ReleaseDossierMetaData.builder()
                
                .releaseArtifacts(convertToReleaseArtifacts(releaseArtifactsList))
                .releaseDate(new DateToOffsetTimeConverter().convert(source.getDate(RELEASE_DATE_FIELD)))
                .supplyChainPath(source.getString(SUPPLY_CHAIN_PATH_FIELD));
        if (source.containsKey(ID_FIELD)) {
            builder.documentId(((ObjectId)source.get(ID_FIELD))
                    .toHexString());
        }
        return builder.build();
    }

    private static List<List<String>> convertToReleaseArtifacts(List<Document> releaseArtifacts) {
        return releaseArtifacts.stream()
                .map(d -> (List<String>) d.get(HASHES))
                .collect(Collectors.toList());
    }

}
