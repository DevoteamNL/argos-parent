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
import java.util.List;
import java.util.Map;

import com.argosnotary.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.adapter.out.mongodb.account.FinishedSessionRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.layout.LayoutMetaBlockRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.account.PersonalAccountRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.layout.ReleaseConfigurationRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.permission.RoleRepositoryImpl;
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
        template.indexOps(RoleRepositoryImpl.COLLECTION).ensureIndex(new Index(RoleRepositoryImpl.ROLE_ID_FIELD, ASC).unique());
        template.indexOps(RoleRepositoryImpl.COLLECTION).ensureIndex(new Index(RoleRepositoryImpl.ROLE_NAME_FIELD, ASC).unique());
        template.save(Role.builder()
                .name(Role.ADMINISTRATOR_ROLE_NAME)
                .permissions(List.of(
                        Permission.READ,
                        Permission.LOCAL_PERMISSION_EDIT,
                        Permission.TREE_EDIT,
                        Permission.VERIFY,
                        Permission.ASSIGN_ROLE
                )).build(), RoleRepositoryImpl.COLLECTION);
        template.save(Role.builder()
                .name(Role.USER_ROLE)
                .permissions(List.of(Permission.PERSONAL_ACCOUNT_READ))
                .build(), RoleRepositoryImpl.COLLECTION);
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

}