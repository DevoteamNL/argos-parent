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
