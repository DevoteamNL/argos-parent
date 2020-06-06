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
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import java.util.Map;

import static com.rabobank.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.SEGMENT_NAME_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.STEP_NAME_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.SUPPLYCHAIN_ID_FIELD;

@ChangeLog
public class ApprovalConfigurationDatabaseChangelog {

    @ChangeSet(order = "001", id = "ApprovalConfigurationChangelog-1-1", author = "michel")
    public void addIndex(MongoTemplate template) {
        createIndex(template, HashedIndex.hashed(SUPPLYCHAIN_ID_FIELD));
        createCompoundIndexOnSupplyChainAndSegmentNameAndStepName(template);
    }

    private void createIndex(MongoTemplate template, IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

    private void createCompoundIndexOnSupplyChainAndSegmentNameAndStepName(MongoTemplate template) {
        createIndex(template, new CompoundIndexDefinition(new Document(Map.of(SUPPLYCHAIN_ID_FIELD, 1, SEGMENT_NAME_FIELD, 1, STEP_NAME_FIELD, 1)))
                .named(SUPPLYCHAIN_ID_FIELD + "_" + SEGMENT_NAME_FIELD + "_" + STEP_NAME_FIELD).unique());
    }

}