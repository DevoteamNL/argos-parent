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
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.permission.Permission;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
public class AccountUserDetailsAdapter extends org.springframework.security.core.userdetails.User {
    private final Account account;
    private final TokenInfo tokenInfo;
    private Set<Permission> globalPermissions = Collections.emptySet();

    public AccountUserDetailsAdapter(PersonalAccount account, TokenInfo tokenInfo, Set<Permission> globalPermissions) {
        super(account.getName(), "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
        this.globalPermissions = globalPermissions;
        this.tokenInfo = tokenInfo;
    }

    public AccountUserDetailsAdapter(ServiceAccount serviceAccount) {
        super(serviceAccount.getName(), "", List.of(new SimpleGrantedAuthority("ROLE_NONPERSONAL")));
        this.account = serviceAccount;
        this.tokenInfo = null;
    }

    public String getId() {
        return account.getAccountId();
    }

    public Account getAccount() {
        return account;
    }

    public Set<Permission> getGlobalPermissions() {
        return globalPermissions;
    }

    public TokenInfo getTokenInfo() {
        return tokenInfo;
    }
}
