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
package com.argosnotary.argos.domain.crypto.signing;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.crypto.Signature;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.operator.OperatorCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignerTest {

    private static final char[] PASSWORD = "password".toCharArray();
    private static final char[] OTHER_PASSWORD = "other password".toCharArray();

    private ServiceAccountKeyPair pair1;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException, OperatorCreationException {
    	KeyPair keyPair = KeyPair.createKeyPair(PASSWORD);
    	pair1 = new ServiceAccountKeyPair(keyPair.getKeyId(), keyPair.getPublicKey(), keyPair.getEncryptedPrivateKey(), null);
    	publicKey = com.argosnotary.argos.domain.crypto.PublicKey.instance(pair1.getPublicKey());
    }

    @Test
    void sign() throws DecoderException, GeneralSecurityException {
        Signature signature = Signer.sign(pair1, PASSWORD, "string to sign");
        assertThat(signature.getKeyId(), is(pair1.getKeyId()));

        java.security.Signature signatureValidator = java.security.Signature.getInstance("SHA384withECDSA");
        signatureValidator.initVerify(publicKey);
        signatureValidator.update("string to sign".getBytes(StandardCharsets.UTF_8));

        assertTrue(signatureValidator.verify(Hex.decodeHex(signature.getSignature())));
    }
    
    @Test
    void signInvalidPassword() throws DecoderException, GeneralSecurityException {
    	Throwable exception = assertThrows(ArgosError.class, () -> {
    		Signer.sign(pair1, OTHER_PASSWORD, "string to sign");
          });
    	assertEquals("unable to read encrypted data: Error finalising cipher", exception.getMessage());
    }
    
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<Signer> constructor = Signer.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
      constructor.setAccessible(true);
      constructor.newInstance();
    }
    
}
