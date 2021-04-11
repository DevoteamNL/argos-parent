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

import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class RequiredNumberOfLinksVerificationTest {

    private static final String STEP_NAME1 = "stepName1";
    private static final String STEP_NAME2 = "stepName2";
    private static final String STEP_NAME3 = "stepName3";
    private static final String KEY_ID_1 = "keyid1";
    private static final String KEY_ID_2 = "keyid2";
    private static final Signature SIGNATURE_1 = Signature.builder().keyId(KEY_ID_1).signature("sig1").build();
    private static final Signature SIGNATURE_2 = Signature.builder().keyId(KEY_ID_2).signature("sig2").build();
    private static final Signature SIGNATURE_3 = Signature.builder().keyId(KEY_ID_1).signature("sig3").build();
    private RequiredNumberOfLinksVerification requiredNumberOfLinksVerification;

    private VerificationContext context;

    private Step step1;    
    
    private Step step2;    
    
    private Step step3;
    
    private LayoutMetaBlock layoutMetaBlock; 
    
    private Layout layout;

    private LinkMetaBlock linkMetaBlock1;

    private LinkMetaBlock linkMetaBlock2;

    private LinkMetaBlock linkMetaBlock3;

    private LinkMetaBlock linkMetaBlock4;

    private LinkMetaBlock linkMetaBlock5;

    @BeforeEach
    void setup() {
        requiredNumberOfLinksVerification = new RequiredNumberOfLinksVerification();

        linkMetaBlock1 = createLinkMetaBlock(SIGNATURE_1, STEP_NAME1);
        linkMetaBlock2 = createLinkMetaBlock(SIGNATURE_2, STEP_NAME1);
        linkMetaBlock3 = createLinkMetaBlock(SIGNATURE_1, STEP_NAME2);
        linkMetaBlock4 = createLinkMetaBlock(SIGNATURE_1, STEP_NAME3);
        linkMetaBlock5 = createLinkMetaBlock(SIGNATURE_3, STEP_NAME3);

        step1 = Step.builder().name(STEP_NAME1).requiredNumberOfLinks(1).build();
        step2 = Step.builder().name(STEP_NAME2).requiredNumberOfLinks(1).build();
        step3 = Step.builder().name(STEP_NAME3).requiredNumberOfLinks(1).build();
        layout = Layout.builder().steps(List.of(step1, step2, step3)).build();
        
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        context = VerificationContext.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(List.of(linkMetaBlock1, linkMetaBlock2, linkMetaBlock3, linkMetaBlock4))
                .productsToVerify(Set.of())
                .build();
    }

    private LinkMetaBlock createLinkMetaBlock(Signature sig, String stepName) {
        return LinkMetaBlock.builder()
                .signature(sig)
                .link(Link.builder()
                        .stepName(stepName)
                        .build())
                .build();
    }

    @Test
    void getPriority() {
        assertThat(requiredNumberOfLinksVerification.getPriority(), is(Verification.Priority.REQUIRED_NUMBER_OF_LINKS));
    }

    @Test
    void verifyWithRequiredNumberOfLinksShouldReturnValid() {
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithNotRequiredNumberOfLinksShouldReturnInValid() {
        step1.setRequiredNumberOfLinks(3);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }

    @Test
    void verifyWithRequiredNumberOfLinks2ShouldReturnValid() {
        step1.setRequiredNumberOfLinks(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithRequiredNumberOfLinks2SignedBySameFunctShouldReturnInValid() {
        step3.setRequiredNumberOfLinks(2);
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
    
    @Test
    void verifyWithSegmentNotFoundReturnInValid() {
        step2.setRequiredNumberOfLinks(2);
        context = VerificationContext.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(List.of(linkMetaBlock1, linkMetaBlock2))
                .productsToVerify(Set.of())
                .build();
        VerificationRunResult result = requiredNumberOfLinksVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}
