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
package com.rabobank.argos.service.adapter.out.mongodb.layout;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import static com.rabobank.argos.service.adapter.out.mongodb.layout.ReleaseConfigurationRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.layout.ReleaseConfigurationRepositoryImpl.SUPPLY_CHAIN_ID_FIELD;

@ChangeLog
public class ReleaseConfigurationDatabaseChangelog {

    @ChangeSet(order = "001", id = "ReleaseConfigurationDatabaseChangelog-1-1", author = "bart")
    public void addIndex(MongoTemplate template) {
        createIndex(template, HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
    }

    private void createIndex(MongoTemplate template, IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

}