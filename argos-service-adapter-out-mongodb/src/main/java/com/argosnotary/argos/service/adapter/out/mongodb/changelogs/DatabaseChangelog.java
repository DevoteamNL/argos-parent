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
package com.argosnotary.argos.service.adapter.out.mongodb.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.HashedIndex;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.io.IOException;
import java.util.Map;

import com.argosnotary.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.account.FinishedSessionRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.layout.LayoutMetaBlockRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.account.PersonalAccountRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.layout.ReleaseConfigurationRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl;

@ChangeLog
public class DatabaseChangelog {

    @ChangeSet(order = "001", id = "ApprovalConfigurationChangelog-1-1", author = "michel")
    public void addApprovalConfigurationIndexes(MongockTemplate template) {
        template.indexOps(ApprovalConfigurationRepositoryImpl.COLLECTION)
            .ensureIndex(HashedIndex.hashed(ApprovalConfigurationRepositoryImpl.SUPPLYCHAIN_ID_FIELD));
        
        IndexDefinition indexDefinition = 
                new CompoundIndexDefinition(
                        new Document(Map.of(
                                ApprovalConfigurationRepositoryImpl.SUPPLYCHAIN_ID_FIELD, 1, 
                                ApprovalConfigurationRepositoryImpl.SEGMENT_NAME_FIELD, 1, 
                                ApprovalConfigurationRepositoryImpl.STEP_NAME_FIELD, 1)))
                    .named(
                            ApprovalConfigurationRepositoryImpl.SUPPLYCHAIN_ID_FIELD + "_" 
                                    + ApprovalConfigurationRepositoryImpl.SEGMENT_NAME_FIELD + "_" 
                                    + ApprovalConfigurationRepositoryImpl.STEP_NAME_FIELD)
                    .unique();
        
        template.indexOps(ApprovalConfigurationRepositoryImpl.COLLECTION).ensureIndex(indexDefinition);
    }

    @ChangeSet(order = "001", id = "FinishedSessionChangelog-1", author = "bart")
    public void addFinishedSessionIndexes(MongockTemplate template) {
        template.indexOps(FinishedSessionRepositoryImpl.COLLECTION)
            .ensureIndex(HashedIndex.hashed(FinishedSessionRepositoryImpl.SESSION_ID_FIELD));
        
        template.indexOps(FinishedSessionRepositoryImpl.COLLECTION)
            .ensureIndex(new Index(FinishedSessionRepositoryImpl.EXPIRATION_DATE_FIELD, ASC));
    }
    
    @ChangeSet(order = "001", id = "HierarchyChangelog-1", author = "michel")
    public void createHierarchyView(MongockTemplate template) throws IOException {
        String createTmpViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-hierarchy-tmp-view-01.json"), UTF_8);
        String createViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-hierarchy-view-01.json"), UTF_8);
        template.dropCollection("hierarchy_tmp");
        template.dropCollection("hierarchy");
        template.executeCommand(createTmpViewCommand);
        template.executeCommand(createViewCommand);

    }
    
    @ChangeSet(order = "001", id = "LabelDatabaseChangelog-1", author = "bart")
    public void addLabelDatabaseIndexes(MongockTemplate template) {
        template.indexOps(LabelRepositoryImpl.COLLECTION)
            .ensureIndex(new Index(LabelRepositoryImpl.LABEL_ID_FIELD, ASC).unique());
        template.indexOps(LabelRepositoryImpl.COLLECTION)
                .ensureIndex(new CompoundIndexDefinition(
                        new Document(Map.of(LabelRepositoryImpl.PARENT_LABEL_ID_FIELD, 1, LabelRepositoryImpl.LABEL_NAME_FIELD, 1)))
                        .named(LabelRepositoryImpl.PARENT_LABEL_ID_FIELD + "_" + LabelRepositoryImpl.LABEL_NAME_FIELD).unique());
    }

    @ChangeSet(order = "001", id = "LayoutDatabaseChangelog-1", author = "bart")
    public void addLayoutDatabaseIndexes(MongockTemplate template) {
        template.indexOps(LayoutMetaBlockRepositoryImpl.COLLECTION)
            .ensureIndex(new Index(LayoutMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD, ASC).unique());
        template.indexOps(LayoutMetaBlockRepositoryImpl.COLLECTION)
            .ensureIndex(HashedIndex.hashed(LayoutMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD));
    }
    
    @ChangeSet(order = "001", id = "LinkDatabaseChangelog-1", author = "bart")
    public void addLinkDatabaseIndexes(MongockTemplate template) {
        template.indexOps(LinkMetaBlockRepositoryImpl.COLLECTION)
            .ensureIndex(HashedIndex.hashed(LinkMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD));
        
        template.indexOps(LinkMetaBlockRepositoryImpl.COLLECTION)
            .ensureIndex(new CompoundIndexDefinition(
                    new Document(LinkMetaBlockRepositoryImpl.LINK_MATERIALS_HASH_FIELD, 1))
                .named(LinkMetaBlockRepositoryImpl.LINK_MATERIALS_HASH_FIELD));
        
        template.indexOps(LinkMetaBlockRepositoryImpl.COLLECTION)
            .ensureIndex(new CompoundIndexDefinition(
                    new Document(LinkMetaBlockRepositoryImpl.LINK_PRODUCTS_HASH_FIELD, 1))
                .named(LinkMetaBlockRepositoryImpl.LINK_PRODUCTS_HASH_FIELD));
        
        template.indexOps(LinkMetaBlockRepositoryImpl.COLLECTION)
            .ensureIndex(new CompoundIndexDefinition(
                    new Document(Map.of(
                            LinkMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD, 1, 
                            LinkMetaBlockRepositoryImpl.SEGMENT_NAME_FIELD, 1, 
                            LinkMetaBlockRepositoryImpl.STEP_NAME_FIELD, 1)))
                .named(LinkMetaBlockRepositoryImpl.SUPPLY_CHAIN_ID_FIELD + "_" 
                            + LinkMetaBlockRepositoryImpl.SEGMENT_NAME_FIELD + "_" 
                        + LinkMetaBlockRepositoryImpl.STEP_NAME_FIELD));
    }

    @ChangeSet(order = "001", id = "PersonalAccountDatabaseChangelog-1", author = "bart")
    public void addPersonalAccountDatabaseIndexes(MongockTemplate template) {
        template.indexOps(PersonalAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(PersonalAccountRepositoryImpl.ACCOUNT_ID, ASC).unique());
        template.indexOps(PersonalAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(PersonalAccountRepositoryImpl.EMAIL, ASC).unique());
        template.indexOps(PersonalAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(PersonalAccountRepositoryImpl.ACTIVE_KEY_ID_FIELD, ASC)
                .partial(PartialIndexFilter.of(new Criteria(PersonalAccountRepositoryImpl.ACTIVE_KEY_ID_FIELD).exists(true))).unique());
        template.indexOps(PersonalAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(PersonalAccountRepositoryImpl.NAME_FIELD, ASC));
        template.indexOps(PersonalAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(PersonalAccountRepositoryImpl.PERMISSIONS_LABEL_ID_FIELD, ASC));
        template.indexOps(PersonalAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(PersonalAccountRepositoryImpl.IN_ACTIVE_KEY_ID_FIELD, ASC)
                .partial(PartialIndexFilter.of(new Criteria(PersonalAccountRepositoryImpl.IN_ACTIVE_KEY_ID_FIELD).exists(true))).unique());
    }

    @ChangeSet(order = "001", id = "ReleaseConfigurationDatabaseChangelog-1-1", author = "bart")
    public void addReleaseConfigurationDatabaseIndexes(MongockTemplate template) {
        template.indexOps(ReleaseConfigurationRepositoryImpl.COLLECTION).ensureIndex(HashedIndex.hashed(ReleaseConfigurationRepositoryImpl.SUPPLY_CHAIN_ID_FIELD));
    }

    @ChangeSet(order = "001", id = "ReleaseDatabaseChangelog-1", author = "michel")
    public void addReleaseDatabaseIndexes(MongockTemplate template) {
        template.indexOps(ReleaseRepositoryImpl.COLLECTION_NAME)
            .ensureIndex(new CompoundIndexDefinition(
                    new Document(Map.of(
                            ReleaseRepositoryImpl.METADATA_RELEASE_ARTIFACTS_ARTIFACTS_HASH_FIELD, 1, 
                            ReleaseRepositoryImpl.SUPPLY_CHAIN_PATH_FIELD, 1)))
                .named(ReleaseRepositoryImpl.METADATA_RELEASE_ARTIFACTS_ARTIFACTS_HASH_FIELD + "_" 
                            + ReleaseRepositoryImpl.SUPPLY_CHAIN_PATH_FIELD));
    }

    @ChangeSet(order = "001", id = "RoleDatabaseChangelog-1", author = "bart")
    public void addRoleDatabaseIndexes(MongockTemplate template) {
    }

    @ChangeSet(order = "001", id = "ServiceAccountDatabaseChangelog-1", author = "bart")
    public void addServiceAccountDatabaseChanges(MongockTemplate template) throws IOException {
        template.indexOps(ServiceAccountRepositoryImpl.COLLECTION).ensureIndex(new Index(ServiceAccountRepositoryImpl.ACCOUNT_ID_FIELD, ASC).unique());
        
        template.indexOps(ServiceAccountRepositoryImpl.COLLECTION)
            .ensureIndex(new CompoundIndexDefinition(
                    new Document(Map.of(
                            ServiceAccountRepositoryImpl.PARENT_LABEL_ID_FIELD, 1, 
                            ServiceAccountRepositoryImpl.ACCOUNT_NAME_FIELD, 1)))
                .named(ServiceAccountRepositoryImpl.PARENT_LABEL_ID_FIELD + "_" 
                            + ServiceAccountRepositoryImpl.ACCOUNT_NAME_FIELD)
                .unique());
        
        template.indexOps(ServiceAccountRepositoryImpl.COLLECTION)
            .ensureIndex(new Index(ServiceAccountRepositoryImpl.ACTIVE_KEY_ID_FIELD, ASC)
                .partial(PartialIndexFilter.of(new Criteria(ServiceAccountRepositoryImpl.ACTIVE_KEY_ID_FIELD)
                        .exists(true))).unique());
        
        String createAccountsKeyInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-service-accounts-key-info-tmp-view-01.json"), UTF_8);
        template.dropCollection("service-accounts-key-info-tmp");
        template.executeCommand(createAccountsKeyInfoViewCommand);
        
        createAccountsKeyInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-accounts-keyinfo-view-01.json"), UTF_8);
        template.dropCollection("accounts-keyinfo");
        template.executeCommand(createAccountsKeyInfoViewCommand);
        
        String createAccountsInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-service-accounts-info-tmp-view-01.json"), UTF_8);
        template.dropCollection("service-accounts-info-tmp");
        template.executeCommand(createAccountsInfoViewCommand);
        
        createAccountsInfoViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-accounts-info-view-01.json"), UTF_8);
        template.dropCollection("accounts-info");
        template.executeCommand(createAccountsInfoViewCommand);
    }

    @ChangeSet(order = "001", id = "SupplyChainDatabaseChangelog-1", author = "bart")
    public void addSupplyChainDatabaseIndexes(MongockTemplate template) {
        template.indexOps(SupplyChainRepositoryImpl.COLLECTION).ensureIndex(HashedIndex.hashed(SupplyChainRepositoryImpl.SUPPLY_CHAIN_ID_FIELD));
        
        template.indexOps(SupplyChainRepositoryImpl.COLLECTION)
            .ensureIndex(new CompoundIndexDefinition(
                    new Document(Map.of(
                            SupplyChainRepositoryImpl.PARENT_LABEL_ID_FIELD, 1, 
                            SupplyChainRepositoryImpl.SUPPLY_CHAIN_NAME_FIELD, 1)))
                .named(SupplyChainRepositoryImpl.PARENT_LABEL_ID_FIELD + "_" 
                            + SupplyChainRepositoryImpl.SUPPLY_CHAIN_NAME_FIELD)
                .unique());
    }

    @ChangeSet(order = "110", id = "DropRoleCollectionDatabaseChangelog-1", author = "gerard")
    public void removeVerifyFromRole(MongockTemplate template) {
        template.dropCollection("roles");
    }

}