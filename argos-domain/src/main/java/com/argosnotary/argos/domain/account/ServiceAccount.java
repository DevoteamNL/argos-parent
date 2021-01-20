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
import com.argosnotary.argos.domain.permission.Permission;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singleton;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ServiceAccount extends Account {
    private static final Set<Permission> permissions;
    static {
        permissions = new HashSet<>();
        permissions.add(Permission.LINK_ADD);
                permissions.add(Permission.RELEASE);
    }
    private String parentLabelId;

    @Builder
    public ServiceAccount(String name, KeyPair activeKeyPair,
            Set<KeyPair> inactiveKeyPairs, String parentLabelId) {
        super(UUID.randomUUID().toString(), name, null, activeKeyPair,
                inactiveKeyPairs == null ? new HashSet<>() : inactiveKeyPairs,
                singleton(LocalPermissions.builder().labelId(parentLabelId)
                        .permissions(permissions).build()));
        this.parentLabelId = parentLabelId;
    }
}
