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
package com.argosnotary.argos.service.adapter.in.rest;

import com.argosnotary.argos.domain.crypto.KeyIdProvider;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.crypto.signing.Signer;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.domain.account.AccountService;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SignatureValidatorServiceTest {
    private char[] PASSPHRASE = "test".toCharArray();

    private static final String KEY_ID = "keyId";
    
    private static final byte[] key = Base64.getDecoder().decode("MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE6UI21H3Ti3fWK98DJPiLxaxHuQBB3P28DeskZWlHQSPi104E7xi49sVMJTDaOHNs9YJVqI2fnvCFtGPk3NTCgA==");
    
    @Mock
    private AccountService accountService;
    
    private SignatureValidatorService service;

	private String keyId;
    private String keyId2;

    private Layout layout;
    private Layout layout2;
    private Layout layout3;

    private Signature layoutSignature;
    private Signature layoutSignature2;
    private Signature linkSignature;
    private Signature linkSignature2;
    
    private KeyPair pair;
    private KeyPair pair2;

    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey;
    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey2;

    private Link link;
    private Link link2;

    @BeforeEach
    void setUp() throws GeneralSecurityException, OperatorCreationException, PemGenerationException {
    	service = new SignatureValidatorService(accountService);
    	
    	Step step = Step.builder().build();
        LayoutSegment segment = LayoutSegment.builder().steps(List.of(step)).build();

        // valid
        pair = KeyPair.createKeyPair(PASSPHRASE);
        keyId = KeyIdProvider.computeKeyId(pair.getPublicKey());
        domainPublicKey = new PublicKey(keyId, pair.getPublicKey());
        layout = Layout.builder()
        		.layoutSegments(List.of(segment))
        		.keys(List.of(domainPublicKey)).build();
        link = Link.builder()
                .products(singletonList(Artifact.builder().hash("hash2").uri("/path/tofile2").build()))
                .materials(singletonList(Artifact.builder().hash("hash").uri("/path/tofile").build())).build();
        layoutSignature = Signer.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(layout));
        linkSignature = Signer.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(link));
        
        // not valid
        pair2 = KeyPair.createKeyPair(PASSPHRASE);
        keyId2 = KeyIdProvider.computeKeyId(pair2.getPublicKey());
        domainPublicKey2 = new PublicKey(keyId2, pair2.getPublicKey());
        layout2 = Layout.builder()
        		.layoutSegments(List.of(segment))
        		.keys(List.of(domainPublicKey, domainPublicKey2)).build();
        layoutSignature2 = Signer.sign(pair2, PASSPHRASE, new JsonSigningSerializer().serialize(layout2));
        linkSignature2 = Signer.sign(pair2, PASSPHRASE, new JsonSigningSerializer().serialize(link));
        // make invalid
        layoutSignature2.setKeyId(keyId);
        linkSignature2.setKeyId(keyId);
    }

    @Test
    void validLayoutSignature() {
        when(accountService.findKeyPairByKeyId(keyId)).thenReturn(Optional.of(pair));
        service.validateSignature(layout, layoutSignature);
    }

    @Test
    void inValidLayoutSignature() throws GeneralSecurityException {
        when(accountService.findKeyPairByKeyId(keyId)).thenReturn(Optional.of(pair));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(layout2, layoutSignature2));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("invalid signature"));
    }

    @Test
    void keyNotFoundLayoutSignature() {
        when(accountService.findKeyPairByKeyId(keyId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(layout, layoutSignature));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is(String.format("signature with keyId [%s] not found", keyId)));
    }
    
    @Test
    void validLinkSignature() {
        when(accountService.findKeyPairByKeyId(keyId)).thenReturn(Optional.of(pair));
        service.validateSignature(link, linkSignature);
    	
    }
    
    @Test
    void inValidLinkSignature() {
        when(accountService.findKeyPairByKeyId(keyId)).thenReturn(Optional.of(pair));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(link, linkSignature2));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is("invalid signature"));
    }
    
    @Test
    void keyNotFoundLinkSignature() {
        when(accountService.findKeyPairByKeyId(keyId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.validateSignature(link, linkSignature));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getReason(), is(String.format("signature with keyId [%s] not found", keyId)));
    }
    

}
