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
package com.argosnotary.argos.domain.crypto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServiceAccountKeyPairTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
    void calculatePassphraseTest() {
        String expected = "c3719225981552ba21838aeba9179a61c0525043e7d24068ca59f811001d14f08d7fc9c71078180f6d21615874e0a652c44c67847b034523e2d40974977a3a10";
        String keyId = "ef07177be75d393163c1589f6dce3c41dd7d4ac4a0cbe4777d2aa45b53342dc6";
        String passphrase = "test";
        String actual = ServiceAccountKeyPair.calculateHashedPassphrase(keyId, passphrase);
        
        assertEquals(expected, actual);
    }
	
	@Test
    void buildServiceAccountTest() {
        ServiceAccountKeyPair account = ServiceAccountKeyPair.builder()
            .encryptedHashedKeyPassphrase("test")
            .encryptedPrivateKey("test".getBytes()).keyId("keyId").build();
        
        assertThat(account.getEncryptedHashedKeyPassphrase(), is("test") );
        assertThat(account.getEncryptedPrivateKey(), is("test".getBytes()));
        assertEquals("keyId", account.getKeyId());
    }
	
	@Test
    void noArgAndSetterTest() {
        ServiceAccountKeyPair account = new ServiceAccountKeyPair();
        account.setEncryptedHashedKeyPassphrase("test");
        account.setEncryptedPrivateKey("test".getBytes());
        account.setKeyId("keyId");
        
        assertThat(account.getEncryptedHashedKeyPassphrase(), is("test") );
        assertThat(account.getEncryptedPrivateKey(), is("test".getBytes()));
        assertEquals("keyId", account.getKeyId());
    }
	
	@Test
    void allArgsTest() {
        ServiceAccountKeyPair account = new ServiceAccountKeyPair("keyId", "pubKey".getBytes(), "privateKey".getBytes(), "hashKey");
        
        assertThat(account.getEncryptedHashedKeyPassphrase(), is("hashKey") );
        assertThat(account.getEncryptedPrivateKey(), is("privateKey".getBytes()));
        assertEquals("keyId", account.getKeyId());
    }
	
	

}
