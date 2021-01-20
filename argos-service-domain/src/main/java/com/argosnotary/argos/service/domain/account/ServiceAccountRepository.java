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
