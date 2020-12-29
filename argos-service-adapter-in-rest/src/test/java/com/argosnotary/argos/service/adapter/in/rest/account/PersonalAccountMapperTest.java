/*
 * Copyright (C) 2020 Argos Notary
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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRole;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
class PersonalAccountMapperTest {

    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String LABEL_ID = "labelId";

    private PersonalAccountMapperImpl mapper;

    private RestRole restRole = RestRole.ADMINISTRATOR;
    
    private PersonalAccount personalAccount;


    @BeforeEach
    void setUp() {
        mapper = new PersonalAccountMapperImpl();
        personalAccount = PersonalAccount.builder().email(EMAIL).name(NAME).roles(Set.of(Role.ADMINISTRATOR)).build();
    }

    @Test
    void convertToRestPersonalAccount() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccount(personalAccount);
        validate(restPersonalAccount);
        assertThat(restPersonalAccount.getRoles(), contains(restRole));
    }

    @Test
    void convertToRestPersonalAccountWithoutRoles() {
        RestPersonalAccount restPersonalAccount = mapper.convertToRestPersonalAccountWithoutRoles(personalAccount);
        assertEquals(null, restPersonalAccount.getRoles());
        validate(restPersonalAccount);
    }

    @Test
    void convertToRestLocalPermission() {
        LocalPermissions localPermissions = LocalPermissions.builder().labelId(LABEL_ID).permissions(Set.of(Permission.values())).build();
        RestLocalPermissions restLocalPermissions = mapper.convertToRestLocalPermissions(localPermissions);
        assertThat(mapper.convertToLocalPermissions(restLocalPermissions), is(localPermissions));
    }

    @Test
    void convertToRestLocalPermissions() {
        LocalPermissions localPermissions = LocalPermissions.builder().labelId(LABEL_ID).permissions(EnumSet.allOf(Permission.class)).build();
        List<RestLocalPermissions> restLocalPermissions = mapper.convertToRestLocalPermissionsList(Set.of(localPermissions));
        assertEquals(mapper.convertToLocalPermissionsSet(restLocalPermissions), Set.of(localPermissions));
    } 


    @Test
    void convertToLocalPermissions() {
        List<RestPermission> restPerms = new ArrayList<>(EnumSet.allOf(RestPermission.class));
        RestLocalPermissions restLocalPermissions = new RestLocalPermissions().labelId(LABEL_ID).permissions(restPerms);
        Set<Permission> perms = EnumSet.allOf(Permission.class);
        assertThat(mapper.convertToLocalPermissionsSet(List.of(restLocalPermissions)), is(Set.of(LocalPermissions.builder().labelId(LABEL_ID).permissions(perms).build())));
    }

    @Test
    void convertToPermissions() {
        List<RestPermission> restPerms = new ArrayList<>(EnumSet.allOf(RestPermission.class));
        Set<Permission> perms = EnumSet.allOf(Permission.class);
        assertThat(mapper.convertToPermissionSet(restPerms), is(perms));
    }

    private void validate(RestPersonalAccount restPersonalAccount) {
        assertThat(restPersonalAccount.getName(), is(NAME));
        assertThat(restPersonalAccount.getId(), hasLength(36));
    }
}