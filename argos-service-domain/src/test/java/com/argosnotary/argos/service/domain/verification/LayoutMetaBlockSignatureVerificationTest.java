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
package com.argosnotary.argos.service.domain.verification;

import com.argosnotary.argos.domain.crypto.KeyIdProvider;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.crypto.signing.Signer;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.GeneralSecurityException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutMetaBlockSignatureVerificationTest {
    private char[] PASSPHRASE = "test".toCharArray();

	private String keyId;
    private String keyId2;

    @Mock
    private VerificationContext context;

    private LayoutMetaBlock layoutMetaBlock;
    private LayoutMetaBlock layoutMetaBlock2;
    private LayoutMetaBlock layoutMetaBlock3;

    private LayoutMetaBlockSignatureVerification verification;

    private Signature signature;
    private Signature signature2;

    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey;
    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey2;

    @BeforeEach
    void setUp() throws GeneralSecurityException, OperatorCreationException, PemGenerationException {
        verification = new LayoutMetaBlockSignatureVerification();
        
        Step step = Step.builder().build();
        LayoutSegment segment = LayoutSegment.builder().steps(List.of(step)).build();

        // valid
        KeyPair pair = KeyPair.createKeyPair(PASSPHRASE);
        keyId = KeyIdProvider.computeKeyId(pair.getPublicKey());
        domainPublicKey = new PublicKey(keyId, pair.getPublicKey());
        Layout layout = Layout.builder()
        		.layoutSegments(List.of(segment))
        		.keys(List.of(domainPublicKey)).build();
        
        signature = Signer.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(layout));
        layoutMetaBlock = LayoutMetaBlock.builder()
        		.signatures(List.of(signature))
                .layout(layout).build();
        
        // key not found
        pair = KeyPair.createKeyPair(PASSPHRASE);
        keyId2 = KeyIdProvider.computeKeyId(pair.getPublicKey());
        domainPublicKey2 = new PublicKey(keyId2, pair.getPublicKey());
        layout = Layout.builder()
        		.layoutSegments(List.of(segment))
        		.keys(List.of(domainPublicKey)).build();
        signature2 = Signer.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(layout));
        layoutMetaBlock2 = LayoutMetaBlock.builder()
        		.signatures(List.of(signature, signature2))
                .layout(layout).build();
        
        // not valid
        layout = Layout.builder()
        		.layoutSegments(List.of(segment))
        		.keys(List.of(domainPublicKey, domainPublicKey2)).build();
        layoutMetaBlock3 = LayoutMetaBlock.builder()
        		.signatures(List.of(signature, signature2))
                .layout(layout).build();
        
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(Verification.Priority.LAYOUT_METABLOCK_SIGNATURE));
    }

    @Test
    void verifyOkay() throws GeneralSecurityException {
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
    	
        assertThat(verification.verify(context).isRunIsValid(), is(true));
    }

    @Test
    void verifyNotOkay() throws GeneralSecurityException {
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock3);
        assertThat(verification.verify(context).isRunIsValid(), is(false));
    }

    @Test
    void verifyKeyNotFound() {
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock2);
        assertThat(verification.verify(context).isRunIsValid(), is(false));
    }
}
