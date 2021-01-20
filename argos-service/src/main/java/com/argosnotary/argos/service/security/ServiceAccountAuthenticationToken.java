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


