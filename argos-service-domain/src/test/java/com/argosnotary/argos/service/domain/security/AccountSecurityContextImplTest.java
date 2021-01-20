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

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSecurityContextImplTest {

    private static final String LABEL_ID = "label_id";
    private AccountSecurityContextImpl context;

    @Mock
    private Authentication authentication;

    @Mock
    private AccountUserDetailsAdapter accountUserDetailsAdapter;

    @Mock
    private Account account;

    @Mock
    private TokenInfo tokenInfo;

    @BeforeEach
    void setUp() {
        context = new AccountSecurityContextImpl();
    }

    @Test
    void getAuthenticatedAccountNotFound() {
        assertThat(context.getAuthenticatedAccount(), is(Optional.empty()));
    }

    @Test
    void getAuthenticatedAccount() {
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThat(context.getAuthenticatedAccount(), is(Optional.of(account)));
    }

    @Test
    void getAllPermissionsWithCorrectIdsShouldReturnCorrectSet() {
        Set<LocalPermissions> localPermissions = Collections.singleton(LocalPermissions.builder()
                .labelId(LABEL_ID)
                .permissions(Set.of(Permission.READ))
                .build());
        when(account.getLocalPermissions()).thenReturn(localPermissions);
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.allLocalPermissions(List.of(LABEL_ID));
        assertThat(permissions, is(Set.of(Permission.READ)));
    }

    @Test
    void getAllPermissionsWithNoAuthenticationShouldReturnEmptySet() {
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.allLocalPermissions(List.of(LABEL_ID));
        assertThat(permissions.isEmpty(), is(true));
    }

    @Test
    void getGlobalPermisionsShouldReturnResult() {
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        when(accountUserDetailsAdapter.getGlobalPermissions()).thenReturn(Set.of(Permission.READ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.getGlobalPermission();
        assertThat(permissions, is(Set.of(Permission.READ)));
    }

    @Test
    void getGlobalPermisionsEmptyAccountShouldReturnEmpty() {
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<Permission> permissions = context.getGlobalPermission();
        assertThat(permissions.isEmpty(), is(true));
    }

    @Test
    void getSessionId() {
        when(accountUserDetailsAdapter.getTokenInfo()).thenReturn(tokenInfo);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(accountUserDetailsAdapter);
        assertThat(context.getTokenInfo().get(), sameInstance(tokenInfo));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}