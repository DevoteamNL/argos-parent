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
package com.argosnotary.argos.domain.account;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    private static final Role ROLE = Role.ADMINISTRATOR;
    protected static final String AZURE = "azure";
    protected static final LocalPermissions LOCAL_PERMISSIONS = LocalPermissions
            .builder()
            .permissions(Collections.singleton(Permission.TREE_EDIT)).labelId("labelId").build();

    @Mock
    private KeyPair activeKeyPair;

    @Mock
    private KeyPair keyPair;

    @Test
    void builder() {
        Set<Role> roles = new HashSet<>();
        roles.add(ROLE);
        PersonalAccount account = PersonalAccount.builder().name(NAME)
                .email(EMAIL)
                .activeKeyPair(activeKeyPair)
                .inactiveKeyPairs(Collections.singleton(keyPair))
                .providerName(AZURE)
                .providerId(PROVIDER_ID)
                .roles(roles)
                .localPermissions(Collections.singleton(LOCAL_PERMISSIONS))
                .build();


        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getName(), is(NAME));
        assertThat(account.getEmail(), is(EMAIL));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProviderName(), is(AZURE));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProviderId(), is(PROVIDER_ID));
        assertThat(account.getRoles(), contains(ROLE));
        
        PersonalAccount account2 = PersonalAccount.builder()
                .build();
        
        account2.addRole(ROLE);
        
        assertThat(account2.getRoles(), contains(ROLE));
        assertThat(account2.getLocalPermissions(), is(empty()));
    }
    
    @Test
    void setterTest() {
        Set<Role> roles = new HashSet<>();
        roles.add(ROLE);
        PersonalAccount account = PersonalAccount.builder().build();
        account.setName(NAME);
        account.setEmail(EMAIL);
        account.setActiveKeyPair(activeKeyPair);
        account.setInactiveKeyPairs(Collections.singleton(keyPair));
        account.setProviderName(AZURE);
        account.setProviderId(PROVIDER_ID);
        account.setRoles(roles);
        account.setLocalPermissions(Collections.singleton(LOCAL_PERMISSIONS));


        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getName(), is(NAME));
        assertThat(account.getEmail(), is(EMAIL));
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getProviderName(), is(AZURE));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getProviderId(), is(PROVIDER_ID));
        assertThat(account.getRoles(), contains(ROLE));
        
        PersonalAccount account2 = PersonalAccount.builder()
                .build();
        
        account2.addRole(ROLE);
        
        assertThat(account2.getRoles(), contains(ROLE));
        assertThat(account2.getLocalPermissions(), is(empty()));
    }
}