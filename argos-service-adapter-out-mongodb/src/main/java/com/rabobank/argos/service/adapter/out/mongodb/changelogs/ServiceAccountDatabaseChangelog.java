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
package com.rabobank.argos.service.adapter.out.mongodb.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.Map;

import static com.rabobank.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl.ACCOUNT_ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl.ACCOUNT_NAME_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl.ACTIVE_KEY_ID_FIELD;
import static com.rabobank.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl.COLLECTION;
import static com.rabobank.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl.PARENT_LABEL_ID_FIELD;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.data.domain.Sort.Direction.ASC;

@ChangeLog
public class ServiceAccountDatabaseChangelog {

    @ChangeSet(order = "001", id = "ServiceAccountDatabaseChangelog-1", author = "bart")
    public void addIndex(MongockTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(ACCOUNT_ID_FIELD, ASC).unique());
        template.indexOps(COLLECTION)
                .ensureIndex(new CompoundIndexDefinition(new Document(Map.of(PARENT_LABEL_ID_FIELD, 1, ACCOUNT_NAME_FIELD, 1)))
                        .named(PARENT_LABEL_ID_FIELD + "_" + ACCOUNT_NAME_FIELD).unique());
    }

    @ChangeSet(order = "002", id = "ServiceAccountDatabaseChangelog-2", author = "bart")
    public void addActiveKeyIndex(MongockTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(ACTIVE_KEY_ID_FIELD, ASC)
                .partial(PartialIndexFilter.of(new Criteria(ACTIVE_KEY_ID_FIELD).exists(true))).unique());
    }

    @SneakyThrows
    @ChangeSet(order = "003", id = "ServiceAccountDatabaseChangelog-3", author = "michel")
    public void createServiceAccountsKeyInfoTempView(MongockTemplate template) {
        String createAccountsKeyInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-service-accounts-key-info-tmp-view-01.json"), UTF_8);
        template.dropCollection("service-accounts-key-info-tmp");
        template.executeCommand(createAccountsKeyInfoViewCommand);
    }

    @SneakyThrows
    @ChangeSet(order = "004", id = "ServiceAccountDatabaseChangelog-4", author = "michel")
    public void createAccountsKeyInfoView(MongockTemplate template) {
        String createAccountsKeyInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-accounts-keyinfo-view-01.json"), UTF_8);
        template.dropCollection("accounts-keyinfo");
        template.executeCommand(createAccountsKeyInfoViewCommand);
    }

    @SneakyThrows
    @ChangeSet(order = "005", id = "ServiceAccountDatabaseChangelog-5", author = "michel")
    public void createServiceAccountsTempView(MongockTemplate template) {
        String createAccountsInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-service-accounts-info-tmp-view-01.json"), UTF_8);
        template.dropCollection("service-accounts-info-tmp");
        template.executeCommand(createAccountsInfoViewCommand);
    }

    @SneakyThrows
    @ChangeSet(order = "006", id = "ServiceAccountDatabaseChangelog-6", author = "michel")
    public void createAccountsInfoView(MongockTemplate template) {
        String createAccountsInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-accounts-info-view-01.json"), UTF_8);
        template.dropCollection("accounts-info");
        template.executeCommand(createAccountsInfoViewCommand);
    }

}