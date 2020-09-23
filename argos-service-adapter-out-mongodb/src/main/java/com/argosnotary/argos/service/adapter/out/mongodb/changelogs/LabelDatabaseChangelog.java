/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
import org.springframework.data.mongodb.core.index.Index;

import java.util.Map;

import static com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl.COLLECTION;
import static com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl.LABEL_ID_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl.LABEL_NAME_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl.PARENT_LABEL_ID_FIELD;
import static org.springframework.data.domain.Sort.Direction.ASC;


@ChangeLog
public class LabelDatabaseChangelog {

    @ChangeSet(order = "001", id = "LabelDatabaseChangelog-1", author = "bart")
    public void addIndex(MongockTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(LABEL_ID_FIELD, ASC).unique());
        template.indexOps(COLLECTION)
                .ensureIndex(new CompoundIndexDefinition(new Document(Map.of(PARENT_LABEL_ID_FIELD, 1, LABEL_NAME_FIELD, 1)))
                        .named(PARENT_LABEL_ID_FIELD + "_" + LABEL_NAME_FIELD).unique());
    }
}