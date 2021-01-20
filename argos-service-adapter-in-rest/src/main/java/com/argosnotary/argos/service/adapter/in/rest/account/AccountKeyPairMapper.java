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
