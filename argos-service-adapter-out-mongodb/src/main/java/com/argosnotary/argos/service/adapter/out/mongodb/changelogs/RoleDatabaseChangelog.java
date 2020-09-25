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
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;
import org.springframework.data.mongodb.core.index.Index;

import java.util.List;

import static com.argosnotary.argos.service.adapter.out.mongodb.permission.RoleRepositoryImpl.COLLECTION;
import static com.argosnotary.argos.service.adapter.out.mongodb.permission.RoleRepositoryImpl.ROLE_ID_FIELD;
import static com.argosnotary.argos.service.adapter.out.mongodb.permission.RoleRepositoryImpl.ROLE_NAME_FIELD;
import static org.springframework.data.domain.Sort.Direction.ASC;


@ChangeLog
public class RoleDatabaseChangelog {

    @ChangeSet(order = "001", id = "RoleDatabaseChangelog-1", author = "bart")
    public void addIndex(MongockTemplate template) {
        template.indexOps(COLLECTION).ensureIndex(new Index(ROLE_ID_FIELD, ASC).unique());
        template.indexOps(COLLECTION).ensureIndex(new Index(ROLE_NAME_FIELD, ASC).unique());
    }

    @ChangeSet(order = "002", id = "RoleDatabaseChangelog-2", author = "bart")
    public void addAdminRole(MongockTemplate template) {
        template.save(Role.builder().name(Role.ADMINISTRATOR_ROLE_NAME)
                .permissions(List.of(Permission.READ,
                        Permission.LOCAL_PERMISSION_EDIT,
                        Permission.TREE_EDIT,
                        Permission.VERIFY,
                        Permission.ASSIGN_ROLE
                )).build(), COLLECTION);
    }

    @ChangeSet(order = "003", id = "RoleDatabaseChangelog-3", author = "michel")
    public void addUserRole(MongockTemplate template) {
        template.save(Role.builder().name(Role.USER_ROLE)
                .permissions(List.of(Permission.PERSONAL_ACCOUNT_READ))
                .build(), COLLECTION);
    }
}