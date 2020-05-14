/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.domain.account;

import com.rabobank.argos.domain.key.KeyPair;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.security.PublicKey;

@Getter
@Setter
public class ServiceAccountKeyPair extends KeyPair {

    private String encryptedHashedKeyPassphrase;

    @Builder
    public ServiceAccountKeyPair(String keyId, byte[] encryptedPrivateKey, PublicKey publicKey, String encryptedHashedKeyPassphrase) {
        super(keyId, encryptedPrivateKey, publicKey);
        this.encryptedHashedKeyPassphrase = encryptedHashedKeyPassphrase;
    }
}
