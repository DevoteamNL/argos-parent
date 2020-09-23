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
package com.argosnotary.argos.service.domain.account;

import lombok.Builder;

import java.util.List;
import java.util.Optional;

@Builder
public class AccountSearchParams {
    private final String roleId;
    private final String localPermissionsLabelId;
    private final String name;
    private final List<String> activeKeyIds;
    private final List<String> inActiveKeyIds;

    public Optional<String> getRoleId() {
        return Optional.ofNullable(roleId);
    }

    public Optional<String> getLocalPermissionsLabelId() {
        return Optional.ofNullable(localPermissionsLabelId);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<List<String>> getActiveKeyIds() {
        return Optional.ofNullable(activeKeyIds);
    }

    public Optional<List<String>> getInActiveKeyIds() {
        return Optional.ofNullable(inActiveKeyIds);
    }
}
