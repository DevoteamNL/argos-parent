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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPublicKey;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestServiceAccountKeyPair;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class AccountKeyPairMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mapping(source = "hashedKeyPassphrase", target = "encryptedHashedKeyPassphrase", qualifiedByName = "encryptHashedKeyPassphrase")
    public abstract ServiceAccountKeyPair convertFromRestKeyPair(RestServiceAccountKeyPair keyPair);

    public abstract RestServiceAccountKeyPair convertToRestKeyPair(ServiceAccountKeyPair keyPair);

    public abstract KeyPair convertFromRestKeyPair(RestKeyPair restKeyPair);

    public abstract RestKeyPair convertToRestKeyPair(KeyPair keyPair);

    @Mapping(source = "keyId", target = "keyId")
    @Mapping(source = "publicKey", target = "publicKey")
    public abstract RestPublicKey convertToRestPublicKey(KeyPair keyPair);
    
    @Mapping(source = "keyId", target = "keyId")
    @Mapping(source = "publicKey", target = "publicKey")
    public abstract PublicKey convertFromRestPublicKey(RestPublicKey publicKey);

    @Named("encryptHashedKeyPassphrase")
    public String encryptHashedKeyPassphrase(String hashedKeyPassphrase) {
        return passwordEncoder.encode(hashedKeyPassphrase);
    }
}
