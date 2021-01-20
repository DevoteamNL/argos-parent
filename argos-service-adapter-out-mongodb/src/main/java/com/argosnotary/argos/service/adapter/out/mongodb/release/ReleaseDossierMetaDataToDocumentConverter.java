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

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.core.convert.converter.Converter;

import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;

import static com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.*;

public class ReleaseDossierMetaDataToDocumentConverter implements Converter<ReleaseDossierMetaData, Document>{
    
    @Override
    public Document convert(ReleaseDossierMetaData releaseDossierMetaData) {
        OffsetTimeToDateConverter converter = new OffsetTimeToDateConverter();
        Document metaData = new Document();
        if (releaseDossierMetaData.getDocumentId() != null) {
            metaData.put(ID_FIELD, new ObjectId(releaseDossierMetaData.getDocumentId()));
        }
        metaData.put(RELEASE_ARTIFACTS_FIELD, convertReleaseArtifactsToDocumentList(releaseDossierMetaData.getReleaseArtifacts()));
        metaData.put(SUPPLY_CHAIN_PATH_FIELD, releaseDossierMetaData.getSupplyChainPath());
        metaData.put(RELEASE_DATE_FIELD, converter.convert(releaseDossierMetaData.getReleaseDate()));
        
        return metaData;
    }
    
    private List<Document> convertReleaseArtifactsToDocumentList(List<List<String>> releaseArtifacts) {
        List<Document> documents = new ArrayList<>();
        releaseArtifacts.forEach(l -> {
            Document document = new Document();
            document.put(ARTIFACTS_HASH, ReleaseDossierMetaData.createHashFromArtifactList(l));
            document.put(HASHES, l);
            documents.add(document);
        });
        
        return documents;
    }

}
