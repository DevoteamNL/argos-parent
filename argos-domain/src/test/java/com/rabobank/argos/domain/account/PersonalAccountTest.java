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
package com.rabobank.argos.domain.account;

import com.rabobank.argos.domain.crypto.KeyPair;
import com.rabobank.argos.domain.permission.LocalPermissions;
import com.rabobank.argos.domain.permission.Permission;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;

@ExtendWith(MockitoExtension.class)
class PersonalAccountTest {

    private static final String EMAIL = "email";
    private static final String NAME = "name";
    private static final String PROVIDER_ID = "providerId";
    private static final String ROLE_ID = "roleId";
    private static final String ROLE_ID2 = "roleId2";
    protected static final String AZURE = "azure";
    protected static final LocalPermissions LOCAL_PERMISSIONS = LocalPermissions
            .builder()
            .permissions(Collections.singletonList(Permission.LAYOUT_ADD)).labelId("labelId").build();

    @Mock
    private KeyPair activeKeyPair;

    @Mock
    private KeyPair keyPair;

    @Test
    void builder() {
        List<String> roles = new ArrayList<>();
        roles.add(ROLE_ID);
        PersonalAccount account = PersonalAccount.builder().name(NAME)
                .email(EMAIL)
                .activeKeyPair(activeKeyPair)
                .inactiveKeyPairs(Collections.singletonList(keyPair))
                .providerName(AZURE)
                .providerId(PROVIDER_ID)
                .roleIds(roles)
                .localPermissions(Collections.singletonList(LOCAL_PERMISSIONS))
                .build();
        
        account.addRoleId(ROLE_ID2);


        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getName(), is(NAME));
        assertThat(account.getEmail(), is(EMAIL));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProviderName(), is(AZURE));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProviderId(), is(PROVIDER_ID));
        assertThat(account.getRoleIds(), contains(ROLE_ID, ROLE_ID2));
        
        PersonalAccount account2 = PersonalAccount.builder()
                .build();
        
        account2.addRoleId(ROLE_ID);
        account2.addRoleId(ROLE_ID2);
        
        assertThat(account2.getRoleIds(), contains(ROLE_ID, ROLE_ID2));
        assertThat(account2.getLocalPermissions(), is(empty()));
    }
    
    @Test
    void setterTest() {
        List<String> roles = new ArrayList<>();
        roles.add(ROLE_ID);
        PersonalAccount account = PersonalAccount.builder().build();
        account.setName(NAME);
        account.setEmail(EMAIL);
        account.setActiveKeyPair(activeKeyPair);
        account.setInactiveKeyPairs(Collections.singletonList(keyPair));
        account.setProviderName(AZURE);
        account.setProviderId(PROVIDER_ID);
        account.setRoleIds(roles);
        account.setLocalPermissions(Collections.singletonList(LOCAL_PERMISSIONS));
        
        account.addRoleId(ROLE_ID2);


        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getName(), is(NAME));
        assertThat(account.getEmail(), is(EMAIL));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProviderName(), is(AZURE));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProviderId(), is(PROVIDER_ID));
        assertThat(account.getRoleIds(), contains(ROLE_ID, ROLE_ID2));
        
        PersonalAccount account2 = PersonalAccount.builder()
                .build();
        
        account2.addRoleId(ROLE_ID);
        account2.addRoleId(ROLE_ID2);
        
        assertThat(account2.getRoleIds(), contains(ROLE_ID, ROLE_ID2));
        assertThat(account2.getLocalPermissions(), is(empty()));
    }
}