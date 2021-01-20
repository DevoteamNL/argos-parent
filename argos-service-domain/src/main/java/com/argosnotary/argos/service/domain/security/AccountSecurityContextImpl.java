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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Component
public class AccountSecurityContextImpl implements AccountSecurityContext {

    @Override
    public Optional<Account> getAuthenticatedAccount() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(authentication -> (AccountUserDetailsAdapter) authentication)
                .map(AccountUserDetailsAdapter::getAccount);
    }

    @Override
    public Optional<TokenInfo> getTokenInfo() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(authentication -> (AccountUserDetailsAdapter) authentication)
                .map(AccountUserDetailsAdapter::getTokenInfo);
    }

    @Override
    public Set<Permission> getGlobalPermission() {
        AccountUserDetailsAdapter authentication = getAccountUserDetailsAdapter();
        if (authentication != null) {
            return authentication.getGlobalPermissions();
        } else {
            return emptySet();
        }
    }

    @Override
    public Set<Permission> allLocalPermissions(List<String> labelIds) {
        AccountUserDetailsAdapter authentication = getAccountUserDetailsAdapter();
        if (authentication != null) {
            Map<String, List<LocalPermissions>> localPermissionsMap = authentication.getAccount().getLocalPermissions()
                    .stream()
                    .collect(Collectors.groupingBy(LocalPermissions::getLabelId));
            return labelIds.stream()
                    .map(labelId -> localPermissionsMap.getOrDefault(labelId, emptyList()))
                    .flatMap(List::stream)
                    .map(LocalPermissions::getPermissions)
                    .flatMap(Set::stream)
                    .collect(toSet());
        } else {
            return emptySet();
        }

    }

    private AccountUserDetailsAdapter getAccountUserDetailsAdapter() {
        return (AccountUserDetailsAdapter) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}

