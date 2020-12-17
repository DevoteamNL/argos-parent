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

import com.argosnotary.argos.service.domain.security.TokenInfo;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
public class PersonalAccountAuthenticationToken extends AbstractAuthenticationToken {

    private final TokenInfo tokenInfo;
    private final UserDetails principal;

    public PersonalAccountAuthenticationToken(TokenInfo tokenInfo, UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.tokenInfo = tokenInfo;
        this.principal = principal;
    }

    @Override
    public String getCredentials() {
        return tokenInfo.getAccountId();
    }

    public TokenInfo getTokenInfo() {
        return tokenInfo;
    }

    @Override
    public UserDetails getPrincipal() {
        return principal;
    }
}
