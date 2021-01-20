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
package com.argosnotary.argos.domain.crypto;

import java.security.PublicKey;

import org.apache.commons.codec.digest.DigestUtils;

public interface KeyIdProvider {
    
    public static String computeKeyId(PublicKey publicKey) {
        return DigestUtils.sha256Hex(publicKey.getEncoded());
    }
    
    public static String computeKeyId(byte[] publicKey) {
        return DigestUtils.sha256Hex(publicKey);
    }
}
