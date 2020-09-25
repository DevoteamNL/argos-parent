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
    private RoleRepository roleRepository;

    @Mock
    private PersonalAccountAuthenticationToken token;

    @BeforeEach
    void setUp() {
        personalAccountUserDetailsService = new PersonalAccountUserDetailsService(personalAccountRepository, roleRepository);
    }

    @Test
    void loadUserById() {
        Role role = Role.builder().name("test").permissions(List.of(Permission.READ)).roleId("id").build();
        when(roleRepository.findByIds(any())).thenReturn(List.of(role));
        when(personalAccountRepository.findByAccountId("id")).thenReturn(Optional.of(personalAccount));
        when(personalAccount.getName()).thenReturn("name");
        when(token.getCredentials()).thenReturn("id");
        UserDetails userDetails = personalAccountUserDetailsService.loadUserByToken(token);
        assertThat(userDetails.getUsername(), is("name"));
    }

    @Test
    void loadUserByIdRolesWithNullPermissionsShouldNotFail() {
        Role role = Role.builder().name("test").build();
        when(roleRepository.findByIds(any())).thenReturn(List.of(role));
        when(personalAccountRepository.findByAccountId("id")).thenReturn(Optional.of(personalAccount));
        when(personalAccount.getName()).thenReturn("name");
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