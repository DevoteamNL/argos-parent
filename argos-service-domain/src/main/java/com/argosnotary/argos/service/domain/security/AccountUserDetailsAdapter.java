/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
