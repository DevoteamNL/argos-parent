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
