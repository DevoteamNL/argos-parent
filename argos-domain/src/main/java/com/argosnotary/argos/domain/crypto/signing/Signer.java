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
