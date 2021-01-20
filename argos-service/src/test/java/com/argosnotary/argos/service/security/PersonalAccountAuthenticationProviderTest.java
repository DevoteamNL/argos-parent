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
package com.argosnotary.argos.service.security;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.domain.security.AccountUserDetailsAdapter;
import com.argosnotary.argos.service.domain.security.TokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountAuthenticationProviderTest {

    @Mock
    private PersonalAccountUserDetailsService personalAccountUserDetailsService;
    private PersonalAccountAuthenticationProvider personalAccountAuthenticationProvider;
    private static final String NOT_AUTHENTICATED = "not authenticated";

    @Mock
    private TokenInfo tokenInfo;

    private AccountUserDetailsAdapter userDetails = new AccountUserDetailsAdapter(PersonalAccount.builder().name("test").build(), tokenInfo, Set.of(Permission.READ));

    private PersonalAccountAuthenticationToken authentication = new PersonalAccountAuthenticationToken(tokenInfo, null, null);
    @Mock
    private LogContextHelper logContextHelper;


    @BeforeEach
    void setup() {
        personalAccountAuthenticationProvider = new PersonalAccountAuthenticationProvider(personalAccountUserDetailsService, logContextHelper);
    }

    @Test
    void testAuthenticateWithValidCredentials() {
        when(personalAccountUserDetailsService.loadUserByToken(authentication)).thenReturn(userDetails);
        Authentication authorized = personalAccountAuthenticationProvider.authenticate(authentication);
        assertThat(authorized.isAuthenticated(), is(true));
        assertThat(authorized.getPrincipal(), sameInstance(userDetails));
        verify(logContextHelper).addAccountInfoToLogContext(userDetails);
    }


    @Test
    void testAuthenticateWithInValidCredentials() {
        when(personalAccountUserDetailsService.loadUserByToken(authentication)).thenThrow(new ArgosError("Personal account with id  not found"));
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> personalAccountAuthenticationProvider.authenticate(authentication));
        assertThat(exception.getMessage(), is(NOT_AUTHENTICATED));
    }

    @Test
    void supports() {
        assertThat(personalAccountAuthenticationProvider.supports(PersonalAccountAuthenticationToken.class), is(true));
    }
}