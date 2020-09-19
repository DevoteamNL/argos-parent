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
package com.rabobank.argos.domain.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.encoders.Hex;

import com.rabobank.argos.domain.ArgosError;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ServiceAccountKeyPair extends KeyPair {
    private String encryptedHashedKeyPassphrase;
    
    @Builder
    public ServiceAccountKeyPair(String keyId, byte[] publicKey, byte[] encryptedPrivateKey, String encryptedHashedKeyPassphrase) {
    	super(keyId, publicKey, encryptedPrivateKey);
        this.encryptedHashedKeyPassphrase = encryptedHashedKeyPassphrase;
    }
    
    public static String calculateHashedPassphrase(String keyId, String passphrase) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(HashAlgorithm.SHA512.getStringValue());
        } catch (NoSuchAlgorithmException e) {
            throw new ArgosError(e.getMessage());
        }
        byte[] passphraseHash = md.digest(passphrase.getBytes());
        byte [] keyIdBytes = keyId.getBytes();
        // salt with keyId
        md.update(keyIdBytes);        
        return Hex.toHexString(md.digest(passphraseHash));
    }
}
