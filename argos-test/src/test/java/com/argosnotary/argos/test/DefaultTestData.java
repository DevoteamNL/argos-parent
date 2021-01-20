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
package com.argosnotary.argos.test;

import com.argosnotary.argos.argos4j.rest.api.model.RestLabel;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class DefaultTestData {
    private String adminToken;
    private Map<String, PersonalAccount> personalAccounts = new HashMap<>();
    private RestLabel defaultRootLabel;
    private Map<String, ServiceAccount> serviceAccount = new HashMap<>();

    @Builder
    @Getter
    @Setter
    public static class PersonalAccount {
        private String accountId;
        private String token;
        private String keyId;
        private String passphrase;
        private byte[] publicKey;
    }

    @Builder
    @Getter
    @Setter
    public static class ServiceAccount {
        private String keyId;
        private String passphrase;
        private String hashedKeyPassphrase;
        private byte[] publicKey;
    }

}

