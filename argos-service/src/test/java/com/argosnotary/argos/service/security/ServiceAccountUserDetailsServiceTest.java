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
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.service.domain.account.ServiceAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAccountUserDetailsServiceTest {
    private static final String USER_NAME = "test";
    private static final String KEY_ID = "keyId";
    @Mock
    private ServiceAccountRepository serviceAccountRepository;
    private ServiceAccount serviceAccount = ServiceAccount.builder().name(USER_NAME).build();
    private ServiceAccountUserDetailsService serviceAccountUserDetailsService;

    @BeforeEach
    void setup() {
        serviceAccountUserDetailsService = new ServiceAccountUserDetailsService(serviceAccountRepository);
    }

    @Test
    void loadUserByIdWithValidIdShouldReturnUserdetails() {
        when(serviceAccountRepository.findByActiveKeyId(anyString())).thenReturn(Optional.of(serviceAccount));
        UserDetails userDetails = serviceAccountUserDetailsService.loadUserById(KEY_ID);
        assertThat(userDetails.getUsername(), is(USER_NAME));
    }

    @Test
    void loadUserByIdWithInValidIdShouldReturnError() {
        when(serviceAccountRepository.findByActiveKeyId(anyString())).thenReturn(Optional.empty());
        Exception exception = assertThrows(ArgosError.class, () -> serviceAccountUserDetailsService.loadUserById("keyId"));
        assertThat(exception.getMessage(), is("Non personal account with keyid keyId not found"));
    }

}