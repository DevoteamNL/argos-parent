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
package com.argosnotary.argos.domain.account;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Role;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import static java.util.UUID.randomUUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
public class PersonalAccount extends Account {
    @Builder
    public PersonalAccount(
            String name,
            String email,
            KeyPair activeKeyPair,
            Set<KeyPair> inactiveKeyPairs,
            String providerName,
            String providerId,
            Set<Role> roles,
            Set<LocalPermissions> localPermissions
    ) {
        super(randomUUID().toString(),
                name,
                email,
                activeKeyPair,
                inactiveKeyPairs == null ? new HashSet<>() : inactiveKeyPairs,
                localPermissions == null ? new HashSet<>() : localPermissions);
        this.providerName = providerName;
        this.providerId = providerId;
        this.roles = roles == null ? new HashSet<>() : roles;
    }

    private String providerName;
    private String providerId;
    private Set<Role> roles;

    public void addRole(Role role) {
        roles = new HashSet<>(this.roles);
        roles.add(role);
    }

}
