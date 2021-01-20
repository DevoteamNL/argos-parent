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
