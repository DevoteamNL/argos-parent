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
package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.crypto.KeyPair;
import com.rabobank.argos.domain.crypto.PublicKeyFactory;
import com.rabobank.argos.domain.crypto.Signature;
import com.rabobank.argos.domain.crypto.signing.SignatureValidator;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.account.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SignatureValidatorServiceTest {

    private static final String KEY_ID = "keyId";
    
    private static final byte[] key = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE6UI21H3Ti3fWK98DJPiLxaxHuQBB3P28DeskZWlHQSPi104E7xi49sVMJTDaOHNs9YJVqI2fnvCFtGPk3NTCgA==");
    
    @Mock
    private SignatureValidator signatureValidator;

    @Mock
    private AccountService accountService;
    private SignatureValidatorService service;

    @Mock
    private Link signable;

    @Mock
    private Signature signature;

    @Mock
    private KeyPair keyPair;

    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
    	publicKey = PublicKeyFactory.instance(key);
        service = new SignatureValidatorService(signatureValidator, accountService);
    }

    @Test
    void validateSignature() throws GeneralSecurityException {
        when(keyPair.getPublicKey()).thenReturn(key);
        when(accountService.findKeyPairByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));
        when(signature.getKeyId()).thenReturn(KEY_ID);

        when(signatureValidator.isValid(signable, signature, publicKey)).thenReturn(true);
        service.validateSignature(signable, signature);
    }

    @Test
    void createInValidSignature() throws GeneralSecurityException {
        when(keyPair.getPublicKey()).thenReturn(key);
        when(accountService.findKeyPairByKeyId(KEY_ID)).thenReturn(Optional.of(keyPair));
        when(signature.getKeyId()).thenReturn(KEY_ID);

        when(signatureValidator.isValid(signable, signature, publicKey)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(signable, signature));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("invalid signature"));
    }

    @Test
    void createSignatureKeyIdNotFound() {
        when(signature.getKeyId()).thenReturn(KEY_ID);
        when(accountService.findKeyPairByKeyId(KEY_ID)).thenReturn(Optional.empty());


        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(signable, signature));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("signature with keyId keyId not found"));
    }

}
