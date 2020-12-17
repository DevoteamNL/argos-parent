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
package com.argosnotary.argos.service.security;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.service.domain.security.AccountUserDetailsAdapter;
import com.argosnotary.argos.service.security.ServiceAccountAuthenticationToken.ServiceAccountCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAccountAuthenticationProviderTest {

    private static final String KEYID = "keyid";
    private static final String PASSWORD = "password";
    private static final String ENCRYPTEDPASSWORD = "encryptedpassword";
    private static final String NOT_AUTHENTICATED = "not authenticated";
    @Mock
    private ServiceAccountUserDetailsService serviceAccountUserDetailsService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LogContextHelper logContextHelper;

    private Authentication authentication;
    private ServiceAccountAuthenticationProvider serviceAccountAuthenticationProvider;
    private AccountUserDetailsAdapter userDetails = new AccountUserDetailsAdapter(ServiceAccount.builder()
            .name("test")
            .activeKeyPair(new ServiceAccountKeyPair(KEYID, null, null, ENCRYPTEDPASSWORD))
            .build());

    @BeforeEach
    void setup() {
        ServiceAccountCredentials credentials = ServiceAccountCredentials
                .builder()
                .keyId(KEYID)
                .password(PASSWORD)
                .build();
        authentication = new ServiceAccountAuthenticationToken(credentials, null);
        serviceAccountAuthenticationProvider = new ServiceAccountAuthenticationProvider(serviceAccountUserDetailsService, passwordEncoder, logContextHelper);
    }

    @Test
    void authenticateWithValidCredentialsShouldReturnAuthenticated() {
        when(serviceAccountUserDetailsService.loadUserById(eq(KEYID))).thenReturn(userDetails);
        when(passwordEncoder.matches(eq(PASSWORD), eq(ENCRYPTEDPASSWORD))).thenReturn(true);
        Authentication authenticatedAccount = serviceAccountAuthenticationProvider.authenticate(authentication);
        assertThat(authenticatedAccount.getPrincipal(), sameInstance(userDetails));
        assertThat(authenticatedAccount.isAuthenticated(), is(true));
        verify(logContextHelper).addAccountInfoToLogContext(userDetails);

    }

    @Test
    void authenticateWithInValidIdShouldReturnUnAuthenticated() {
        when(serviceAccountUserDetailsService.loadUserById(eq(KEYID))).thenThrow(new ArgosError("service account not found"));
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> serviceAccountAuthenticationProvider.authenticate(authentication));
        assertThat(exception.getMessage(), is(NOT_AUTHENTICATED));
    }

    @Test
    void authenticateWithInValidPasswordShouldThrowBadCredentials() {
        when(serviceAccountUserDetailsService.loadUserById(eq(KEYID))).thenReturn(userDetails);
        when(passwordEncoder.matches(eq(PASSWORD), eq(ENCRYPTEDPASSWORD))).thenReturn(false);
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> serviceAccountAuthenticationProvider.authenticate(authentication));
        assertThat(exception.getMessage(), is(NOT_AUTHENTICATED));
    }

    @Test
    void supports() {
        assertThat(serviceAccountAuthenticationProvider
                        .supports(ServiceAccountAuthenticationToken.class),
                is(true));
    }
}