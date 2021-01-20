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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8EncryptorBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.OutputEncryptor;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bouncycastle.util.io.pem.PemObject;

import com.argosnotary.argos.domain.ArgosError;

@Getter
@Setter
@ToString(callSuper=true)
@NoArgsConstructor
public class KeyPair extends PublicKey implements Serializable {
    private byte[] encryptedPrivateKey;
    
    public KeyPair(String keyId, byte[] publicKey, byte[] encryptedPrivateKey) {
    	super(keyId, publicKey);
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public PrivateKey decryptPrivateKey(char[] keyPassphrase) {
        try {
            PKCS8EncryptedPrivateKeyInfo encPKInfo = new PKCS8EncryptedPrivateKeyInfo(this.encryptedPrivateKey);
            InputDecryptorProvider decProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().setProvider("BC").build(keyPassphrase);
            PrivateKeyInfo pkInfo = encPKInfo.decryptPrivateKeyInfo(decProv);
            return new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pkInfo);
        } catch (IOException | PKCSException | OperatorCreationException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }
    
	public static KeyPair createKeyPair(char[] passphrase) throws NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyAlgorithm.EC.name());
		java.security.KeyPair keyPair = generator.generateKeyPair();
        JceOpenSSLPKCS8EncryptorBuilder encryptorBuilder = new JceOpenSSLPKCS8EncryptorBuilder(PKCS8Generator.AES_256_CBC).setProvider("BC");  
        OutputEncryptor encryptor = encryptorBuilder
        		.setRandom(new SecureRandom())
        		.setPasssword(passphrase).build();
      
        JcaPKCS8Generator gen2 = new JcaPKCS8Generator(keyPair.getPrivate(), encryptor);  
        PemObject obj2 = gen2.generate();
        return new KeyPair(KeyIdProvider.computeKeyId(keyPair.getPublic()), 
        		keyPair.getPublic().getEncoded(), obj2.getContent());
	}
}
