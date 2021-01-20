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

import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

@ExtendWith(MockitoExtension.class)
class ServiceAccountTest {

    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String NAME = "name";

    @Mock
    private ServiceAccountKeyPair activeKeyPair;

    @Mock
    private ServiceAccountKeyPair keyPair;

    @Test
    void builder() {
        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.LINK_ADD);
        permissions.add(Permission.RELEASE);
        ServiceAccount account = ServiceAccount.builder().name(NAME)
                .parentLabelId(PARENT_LABEL_ID)
                .activeKeyPair(activeKeyPair)
                .inactiveKeyPairs(Collections.singleton(keyPair))
                .build();
        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getEmail(), nullValue());
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getParentLabelId(), is(PARENT_LABEL_ID));
        assertThat(account.getLocalPermissions(), contains(LocalPermissions.builder().labelId(PARENT_LABEL_ID).permissions(permissions).build()));
    }
    
    @Test
    void setterTest() {
        ServiceAccount account = ServiceAccount.builder().build();
        account.setName(NAME);
        account.setParentLabelId(PARENT_LABEL_ID);
        account.setActiveKeyPair(activeKeyPair);
        account.setInactiveKeyPairs(Collections.singleton(keyPair));
        
        assertThat(account.getAccountId(), hasLength(36));
        assertThat(account.getEmail(), nullValue());
        assertThat(account.getActiveKeyPair(), sameInstance(activeKeyPair));
        assertThat(account.getInactiveKeyPairs(), contains(keyPair));
        assertThat(account.getParentLabelId(), is(PARENT_LABEL_ID));
    }
}