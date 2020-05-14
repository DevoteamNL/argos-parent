/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.security;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
public class ServiceAccountAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final UserDetails principal;
    private final ServiceAccountCredentials serviceAccountCredentials;

    ServiceAccountAuthenticationToken(ServiceAccountCredentials serviceAccountCredentials, UserDetails principal) {
        super(principal, serviceAccountCredentials);
        this.principal = principal;
        this.serviceAccountCredentials = serviceAccountCredentials;
    }

    ServiceAccountAuthenticationToken(ServiceAccountCredentials serviceAccountCredentials, UserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(principal, serviceAccountCredentials, authorities);
        this.principal = principal;
        this.serviceAccountCredentials = serviceAccountCredentials;
    }

    ServiceAccountCredentials getServiceAccountCredentials() {
        return serviceAccountCredentials;
    }

    @Override
    public UserDetails getPrincipal() {
        return principal;
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    static class ServiceAccountCredentials implements Serializable {
        private String keyId;
        private String password;
    }
}


