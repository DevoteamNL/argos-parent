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
package com.argosnotary.argos.domain.crypto.signing;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Signer {

    public static Signature sign(KeyPair keyPair, char[] keyPassphrase, String jsonRepresentation) {
    	Signature sig = Signature.builder().keyId(keyPair.getKeyId()).build();
    	try {
			sig.setSignature(createSignature(keyPair.decryptPrivateKey(keyPassphrase), jsonRepresentation, sig.getAlgorithm()));
		} catch (GeneralSecurityException e) {
            throw new ArgosError(e.getMessage(), e);
		}
        return sig;
    }

    private static String createSignature(PrivateKey privateKey, String jsonRepr, SignatureAlgorithm algorithm) throws GeneralSecurityException {
        java.security.Signature privateSignature = java.security.Signature.getInstance(algorithm.getStringValue());
        privateSignature.initSign(privateKey);
        privateSignature.update(jsonRepr.getBytes(StandardCharsets.UTF_8));
        return Hex.encodeHexString(privateSignature.sign());
    }
}
