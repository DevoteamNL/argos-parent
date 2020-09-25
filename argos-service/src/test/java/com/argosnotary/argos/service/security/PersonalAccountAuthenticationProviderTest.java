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