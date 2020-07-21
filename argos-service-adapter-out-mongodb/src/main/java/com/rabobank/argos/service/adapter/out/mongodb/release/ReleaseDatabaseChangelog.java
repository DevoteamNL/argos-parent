/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.out.mongodb.release;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;

import java.util.Map;

import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.METADATA_RELEASE_ARTIFACTS_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.SUPPLY_CHAIN_PATH_FIELD;
import static org.springframework.data.domain.Sort.Direction.ASC;


@ChangeLog
public class ReleaseDatabaseChangelog {

    @ChangeSet(order = "001", id = "ReleaseDatabaseChangelog-1", author = "michel")
    public void addIndex(MongoTemplate template) {
        template.indexOps(ReleaseRepositoryImpl.COLLECTION_NAME)
                .ensureIndex(new Index(METADATA_RELEASE_ARTIFACTS_FIELD, ASC));
        template.indexOps(ReleaseRepositoryImpl.COLLECTION_NAME)
                .ensureIndex(new CompoundIndexDefinition(new Document(Map.of(METADATA_RELEASE_ARTIFACTS_FIELD, 1, SUPPLY_CHAIN_PATH_FIELD, 1)))
                        .named(METADATA_RELEASE_ARTIFACTS_FIELD + "_" + SUPPLY_CHAIN_PATH_FIELD));
    }

}