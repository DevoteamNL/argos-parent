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

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.index.IndexOperations;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

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
import com.argosnotary.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class DatabaseChangelogTest {

    @Mock
    private IndexOperations indexOperations;

    @Mock
    private MongockTemplate template;
    
    @Mock
    Document response;
    
    @Captor
    private ArgumentCaptor<Role> roleArgumentCaptor;


    @Test
    void addApprovalConfigurationIndexes() {
        when(template.indexOps(ApprovalConfigurationRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addApprovalConfigurationIndexes(template);
        verify(template, times(2)).indexOps(ApprovalConfigurationRepositoryImpl.COLLECTION);
    }

    @Test
    void addFinishedSessionIndexes() {
        when(template.indexOps(FinishedSessionRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addFinishedSessionIndexes(template);
        verify(template, times(2)).indexOps(FinishedSessionRepositoryImpl.COLLECTION);
    }

    @Test
    void createHierarchyView() throws IOException {
        when(template.executeCommand(any(String.class))).thenReturn(response);
        new DatabaseChangelog().createHierarchyView(template);
        verify(template, times(2)).executeCommand(any(String.class));
    }

    @Test
    void addLabelDatabaseIndexes() {
        when(template.indexOps(LabelRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addLabelDatabaseIndexes(template);
        verify(template, times(2)).indexOps(LabelRepositoryImpl.COLLECTION);
    }

    @Test
    void addLayoutDatabaseIndexes() {
        when(template.indexOps(LayoutMetaBlockRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addLayoutDatabaseIndexes(template);
        verify(template, times(2)).indexOps(LayoutMetaBlockRepositoryImpl.COLLECTION);
    }

    @Test
    void addLinkDatabaseIndexes() {
        when(template.indexOps(LinkMetaBlockRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addLinkDatabaseIndexes(template);
        verify(template, times(4)).indexOps(LinkMetaBlockRepositoryImpl.COLLECTION);
    }

    @Test
    void addPersonalAccountDatabaseIndexes() {
        when(template.indexOps(PersonalAccountRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addPersonalAccountDatabaseIndexes(template);
        verify(template, times(6)).indexOps(PersonalAccountRepositoryImpl.COLLECTION);
    }

    @Test
    void addReleaseConfigurationDatabaseIndexes() {
        when(template.indexOps(ReleaseConfigurationRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addReleaseConfigurationDatabaseIndexes(template);
        verify(template, times(1)).indexOps(ReleaseConfigurationRepositoryImpl.COLLECTION);
    }

    @Test
    void addReleaseDatabaseIndexes() {
        when(template.indexOps(ReleaseRepositoryImpl.COLLECTION_NAME)).thenReturn(indexOperations);
        new DatabaseChangelog().addReleaseDatabaseIndexes(template);
        verify(indexOperations, times(1)).ensureIndex(any());
    }

    @Test
    void addServiceAccountDatabaseChanges() throws IOException {
        when(template.indexOps(ServiceAccountRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addServiceAccountDatabaseChanges(template);
        verify(template, times(3)).indexOps(ServiceAccountRepositoryImpl.COLLECTION);
    }

    @Test
    void addSupplyChainDatabaseIndexes() {
        when(template.indexOps(SupplyChainRepositoryImpl.COLLECTION)).thenReturn(indexOperations);
        new DatabaseChangelog().addSupplyChainDatabaseIndexes(template);
        verify(indexOperations, times(2)).ensureIndex(any());
    }
}