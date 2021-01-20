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
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.domain.account.PersonalAccountRepository;
import com.argosnotary.argos.service.domain.permission.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountUserDetailsServiceTest {

    @Mock
    private PersonalAccountRepository personalAccountRepository;
    private PersonalAccountUserDetailsService personalAccountUserDetailsService;

    @Mock
    private PersonalAccount personalAccount;

    @Mock
    private PersonalAccountAuthenticationToken token;

    @BeforeEach
    void setUp() {
        personalAccountUserDetailsService = new PersonalAccountUserDetailsService(personalAccountRepository);
    }

    @Test
    void loadUserById() {
        Role role = Role.ADMINISTRATOR;
        when(personalAccountRepository.findByAccountId("id")).thenReturn(Optional.of(personalAccount));
        when(personalAccount.getName()).thenReturn("name");
        when(personalAccount.getRoles()).thenReturn(Set.of(role));
        when(token.getCredentials()).thenReturn("id");
        UserDetails userDetails = personalAccountUserDetailsService.loadUserByToken(token);
        assertThat(userDetails.getUsername(), is("name"));
    }

    @Test
    void loadUserByIdNotFound() {
        when(personalAccountRepository.findByAccountId("id")).thenReturn(Optional.empty());
        when(token.getCredentials()).thenReturn("id");
        ArgosError argosError = assertThrows(ArgosError.class, () -> personalAccountUserDetailsService.loadUserByToken(token));
        assertThat(argosError.getMessage(), is("Personal account with id id not found"));
    }
}