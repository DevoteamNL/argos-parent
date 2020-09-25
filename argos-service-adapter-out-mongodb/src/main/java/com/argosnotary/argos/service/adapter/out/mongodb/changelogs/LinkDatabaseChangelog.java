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
package com.argosnotary.argos.service.adapter.out.mongodb.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

import org.bson.Document;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.IndexDefinition;

import java.util.Map;

import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.COLLECTION;
import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.LINK_MATERIALS_HASH_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.LINK_PRODUCTS_HASH_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.SEGMENT_NAME_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.STEP_NAME_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD;


@ChangeLog
public class LinkDatabaseChangelog {

    @ChangeSet(order = "001", id = "LinkDatabaseChangelog-1", author = "bart")
    public void addIndexes(MongockTemplate template) {
        createIndex(template, HashedIndex.hashed(SUPPLY_CHAIN_ID_FIELD));
        createIndex(template, new CompoundIndexDefinition(new Document(LINK_MATERIALS_HASH_FIELD, 1)).named(LINK_MATERIALS_HASH_FIELD));
        createIndex(template, new CompoundIndexDefinition(new Document(LINK_PRODUCTS_HASH_FIELD, 1)).named(LINK_PRODUCTS_HASH_FIELD));
        createCompoundIndexOnSupplyChainAndStepName(template);
    }

    private void createCompoundIndexOnSupplyChainAndStepName(MongockTemplate template) {
        createIndex(template, new CompoundIndexDefinition(new Document(Map.of(SUPPLY_CHAIN_ID_FIELD, 1, SEGMENT_NAME_FIELD, 1, STEP_NAME_FIELD, 1)))
                .named(SUPPLY_CHAIN_ID_FIELD + "_" + SEGMENT_NAME_FIELD + "_" + STEP_NAME_FIELD));
    }

    private void createIndex(MongockTemplate template, IndexDefinition indexDefinition) {
        template.indexOps(COLLECTION).ensureIndex(indexDefinition);
    }

}