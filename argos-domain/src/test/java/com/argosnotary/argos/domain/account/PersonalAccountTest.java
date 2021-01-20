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