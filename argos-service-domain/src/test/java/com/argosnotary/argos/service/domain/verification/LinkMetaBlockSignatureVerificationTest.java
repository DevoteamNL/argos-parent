/*
 * Copyright (C) 2020 Argos Notary
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
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkMetaBlockSignatureVerificationTest {
    private char[] PASSPHRASE = "test".toCharArray();

    private String keyId;
    private String keyId2;

    @Mock
    private VerificationContext context;

    private LinkMetaBlock linkMetaBlock;
    private LinkMetaBlock linkMetaBlock2;

    private LinkMetaBlockSignatureVerification verification;

    private Link link;

    private Signature signature;
    private Signature signature2;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    private Layout layout;

    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey;
    private com.argosnotary.argos.domain.crypto.PublicKey domainPublicKey2;

    @BeforeEach
    void setUp() throws OperatorCreationException, PemGenerationException, GeneralSecurityException {
        verification = new LinkMetaBlockSignatureVerification();

        link = Link.builder()
                .products(singletonList(Artifact.builder().hash("hash2").uri("/path/tofile2").build()))
                .materials(singletonList(Artifact.builder().hash("hash").uri("/path/tofile").build())).build();

        KeyPair pair = KeyPair.createKeyPair(PASSPHRASE);
        keyId = KeyIdProvider.computeKeyId(pair.getPublicKey());
        domainPublicKey = new PublicKey(keyId, pair.getPublicKey());
        signature = Signer.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(link));
        
        pair = KeyPair.createKeyPair(PASSPHRASE);
        keyId2 = KeyIdProvider.computeKeyId(pair.getPublicKey());
        domainPublicKey2 = new PublicKey(keyId2, pair.getPublicKey());
        signature2 = Signer.sign(pair, PASSPHRASE, new JsonSigningSerializer().serialize(link));
        
        // make invalid
        signature2.setKeyId(keyId);

        linkMetaBlock = LinkMetaBlock.builder()
                .signature(signature)
                .link(link).build();
        linkMetaBlock2 = LinkMetaBlock.builder()
                .signature(signature2)
                .link(link).build();
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(Verification.Priority.LINK_METABLOCK_SIGNATURE));
    }

    @Test
    void verifyOkay() throws GeneralSecurityException {
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock));
        layout = Layout.builder().keys(List.of(domainPublicKey)).build();
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(Collections.emptyList());
    }

    @Test
    void verifyNotValid() throws GeneralSecurityException {
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock2));
        layout = Layout.builder().keys(List.of(domainPublicKey, domainPublicKey2)).build();
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(List.of(linkMetaBlock2));
    }

    @Test
    void verifyKeyNotFound() throws GeneralSecurityException {
        when(context.getLinkMetaBlocks()).thenReturn(List.of(linkMetaBlock));
        layout = Layout.builder().keys(Collections.emptyList()).build();
        when(context.getLayoutMetaBlock()).thenReturn(layoutMetaBlock);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        assertThat(verification.verify(context).isRunIsValid(), is(true));
        verify(context).removeLinkMetaBlocks(List.of(linkMetaBlock));
    }
}
