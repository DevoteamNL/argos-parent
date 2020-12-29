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
