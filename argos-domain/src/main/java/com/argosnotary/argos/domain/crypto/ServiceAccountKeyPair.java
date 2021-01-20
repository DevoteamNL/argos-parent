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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.util.encoders.Hex;

import com.argosnotary.argos.domain.ArgosError;

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
