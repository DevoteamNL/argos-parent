/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
package com.argosnotary.argos.domain.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PublicKey {
    private String keyId;
    private byte[] publicKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    
    @JsonIgnore
    public java.security.PublicKey getJavaPublicKey() throws GeneralSecurityException, IOException {
    	return PublicKey.instance(publicKey);
    }

    public static java.security.PublicKey instance(byte[] encodedKey) throws GeneralSecurityException, IOException {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = null;
        try (ASN1InputStream aIn = new ASN1InputStream(encodedKey)) {
            SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(aIn.readObject());
            keyFactory = KeyFactory.getInstance(info.getAlgorithm().getAlgorithm().getId(), "BC");
        }
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }
}
