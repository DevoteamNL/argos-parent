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
package com.argosnotary.argos.domain.permission;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum Role {

    ADMINISTRATOR;

    public Set<Permission> getPermissions() {
        if (this == ADMINISTRATOR) {
            Set<Permission> permissions = new HashSet<>();
            permissions.addAll(Arrays.asList(
                    Permission.READ, 
                    Permission.LOCAL_PERMISSION_EDIT, 
                    Permission.TREE_EDIT,
                    Permission.ASSIGN_ROLE));
            return permissions;
        } else {
            throw new AssertionError("Unknown role: " + this);
        }

    }
}
