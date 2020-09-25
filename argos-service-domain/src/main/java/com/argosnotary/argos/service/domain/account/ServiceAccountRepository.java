/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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

import com.argosnotary.argos.domain.account.ServiceAccount;

import java.util.Optional;

public interface ServiceAccountRepository {
    void save(ServiceAccount serviceAccount);

    Optional<ServiceAccount> findById(String accountId);

    Optional<ServiceAccount> findByActiveKeyId(String activeKeyId);

    void update(ServiceAccount serviceAccount);

    void delete(String accountId);

    boolean activeKeyExists(String activeKeyId);

    Optional<String> findParentLabelIdByAccountId(String accountId);

    boolean exists(String serviceAccountId);
}
