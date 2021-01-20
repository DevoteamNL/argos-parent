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
