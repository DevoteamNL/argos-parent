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
package com.rabobank.argos.domain.crypto.signing;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.crypto.Signature;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.link.Link;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SignatureValidator {

    public boolean isValid(Link link, Signature signature, PublicKey publicKey) {
        return isValid(new JsonSigningSerializer().serialize(link), signature, publicKey);
    }

    public boolean isValid(Layout layout, Signature signature, PublicKey publicKey) {
        return isValid(new JsonSigningSerializer().serialize(layout), signature, publicKey);
    }

    private boolean isValid(String signableJson, Signature signature, PublicKey publicKey) {
        try {
            java.security.Signature publicSignature = java.security.Signature.getInstance(signature.getAlgorithm().getStringValue());
            publicSignature.initVerify(publicKey);
            publicSignature.update(signableJson.getBytes(UTF_8));
            byte[] signatureBytes = Hex.decodeHex(signature.getSignature());
            return publicSignature.verify(signatureBytes);
        } catch (GeneralSecurityException | DecoderException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
}
