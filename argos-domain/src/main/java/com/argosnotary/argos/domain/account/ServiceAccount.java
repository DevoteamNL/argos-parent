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
package com.argosnotary.argos.domain.account;

import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
public class ServiceAccount extends Account {
    private String parentLabelId;

    @Builder
    public ServiceAccount(String name, ServiceAccountKeyPair activeKeyPair, List<ServiceAccountKeyPair> inactiveKeyPairs, String parentLabelId) {
        super(UUID.randomUUID().toString(), name, null, activeKeyPair, inactiveKeyPairs == null ? emptyList() : inactiveKeyPairs, singletonList(LocalPermissions.builder().labelId(parentLabelId)
                .permissions(Arrays.asList(Permission.LINK_ADD, Permission.VERIFY, Permission.RELEASE)).build()));
        this.parentLabelId = parentLabelId;
    }
}
