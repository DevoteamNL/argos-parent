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
import com.argosnotary.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PersonalAccountUserDetailsService {

    private final PersonalAccountRepository personalAccountRepository;
    
    private static Stream<? extends Permission> apply(Role role) {
        return role.getPermissions().stream();
    }

    UserDetails loadUserByToken(PersonalAccountAuthenticationToken token) {
        PersonalAccount personalAccount = personalAccountRepository.findByAccountId(token.getCredentials())
                .orElseThrow(() -> new ArgosError("Personal account with id " + token.getCredentials() + " not found"));
        Set<Permission> globalPermissions = personalAccount.getRoles()
                .stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(PersonalAccountUserDetailsService::apply)
                .collect(Collectors.toSet());
        return new AccountUserDetailsAdapter(personalAccount, token.getTokenInfo(), globalPermissions);
    }

}