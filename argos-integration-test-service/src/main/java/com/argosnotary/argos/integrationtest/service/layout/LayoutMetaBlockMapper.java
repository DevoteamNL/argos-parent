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
package com.argosnotary.argos.integrationtest.service.layout;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestLayoutMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

@Mapper(componentModel = "spring", uses = {StepMapper.class})
public interface LayoutMetaBlockMapper {


    @Mapping(target = "supplyChainId", ignore = true)
    LayoutMetaBlock convertFromRestLayoutMetaBlock(RestLayoutMetaBlock metaBlock);

    RestLayoutMetaBlock convertToRestLayoutMetaBlock(LayoutMetaBlock metaBlock);

    @Mapping(source = "key", target = "key")
    default byte[] convertPublicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
}
