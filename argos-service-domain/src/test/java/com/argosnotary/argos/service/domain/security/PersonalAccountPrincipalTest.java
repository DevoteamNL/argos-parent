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
package com.argosnotary.argos.service.domain.security;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.permission.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountPrincipalTest {

    @Mock
    private PersonalAccount personalAccount;

    private Set<Permission> globalPermissions;

    @Mock
    private TokenInfo tokenInfo;

    @BeforeEach
    void setUp() {
        when(personalAccount.getName()).thenReturn("name");
        globalPermissions = Set.of(Permission.READ);
    }

    @Test
    void getId() {
        when(personalAccount.getAccountId()).thenReturn("id");
        assertThat(new AccountUserDetailsAdapter(personalAccount, tokenInfo, globalPermissions).getId(), is("id"));
    }

    @Test
    void getPassword() {
        assertThat(new AccountUserDetailsAdapter(personalAccount, tokenInfo, globalPermissions).getPassword(), is(""));
    }

    @Test
    void getAuthorities() {
        assertThat(new AccountUserDetailsAdapter(personalAccount, tokenInfo, globalPermissions).getAuthorities(), contains(new SimpleGrantedAuthority("ROLE_USER")));
    }
}