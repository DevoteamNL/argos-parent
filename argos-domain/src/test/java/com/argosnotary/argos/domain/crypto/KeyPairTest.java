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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.crypto.signing.Signer;

class KeyPairTest {
	@Test
	void createKeyPairAndSignature() throws OperatorCreationException, GeneralSecurityException, IOException {
		String passphrase = "test";
		KeyPair keyPair = KeyPair.createKeyPair("test".toCharArray());
		byte[] encodedKey = Base64.getEncoder().encode(keyPair.getEncryptedPrivateKey());
		String jsonTempl = "{\n" + 
				"  \"keyId\": \"%s\",\n" + 
				"  \"publicKey\": \"%s\",\n" + 
				"  \"encryptedPrivateKey\": \"%s\",\n" +
				"  \"passphrase\": \"%s\"\n" +
				"}";
		System.out.println("id:           "+keyPair.getKeyId());
		System.out.println("encryptedKey: "+new String(encodedKey));
		System.out.println("publicKey:    "+new String(Base64.getEncoder().encode(keyPair.getPublicKey())));
		System.out.println(String.format(jsonTempl, 
				keyPair.getKeyId(), 
				new String(Base64.getEncoder().encode(keyPair.getPublicKey())),
				new String(Base64.getEncoder().encode(keyPair.getEncryptedPrivateKey())), 
				passphrase));
		Signature signature = Signer.sign(keyPair, passphrase.toCharArray(), "zomaar");
		System.out.println("signature: "+new String(Base64.getEncoder().encode(signature.getSignature().getBytes())));
		assertThat(signature.getKeyId(), is(keyPair.getKeyId()));
		assertThat(signature.getKeyAlgorithm(), is(KeyAlgorithm.valueOf(keyPair.getJavaPublicKey().getAlgorithm())));
		
	}
	
	@Test
    void toStringTest() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, PemGenerationException {
        KeyPair keyPair = new KeyPair("keyId", "publicKey".getBytes(), "encryptedPrivateKey".getBytes());
        assertThat(keyPair.toString(), is("KeyPair(super=PublicKey(keyId=keyId, publicKey=[112, 117, 98, 108, 105, 99, 75, 101, 121]), encryptedPrivateKey=[101, 110, 99, 114, 121, 112, 116, 101, 100, 80, 114, 105, 118, 97, 116, 101, 75, 101, 121])"));
        
    }
	
	@Test
    void setterAndConstructorTest() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, PemGenerationException {
        KeyPair keyPair = new KeyPair();
        keyPair.setKeyId("keyId");
        keyPair.setPublicKey("publicKey".getBytes());
        keyPair.setEncryptedPrivateKey("encryptedPrivateKey".getBytes());
        assertThat(keyPair.getKeyId(), is("keyId"));
        assertThat(new String(keyPair.getEncryptedPrivateKey()), is("encryptedPrivateKey"));
        assertThat(new String(keyPair.getPublicKey()), is("publicKey"));
        
        
    }

}
