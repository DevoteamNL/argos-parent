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

import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StepAuthorizedKeyIdVerificationTest {

    private static final String STEP_NAME = "stepName";
    private static final String SEGMENT_NAME = "segmentName";

    private StepAuthorizedKeyIdVerification stepAuthorizedKeyIdVerification;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VerificationContext context;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Step step;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LinkMetaBlock linkMetaBlock;

    @Captor
    ArgumentCaptor<List<LinkMetaBlock>> listArgumentCaptor;

    @Mock
    private PublicKey publicKey;

    @BeforeEach
    void setup() {
        stepAuthorizedKeyIdVerification = new StepAuthorizedKeyIdVerification();
    }

    @Test
    void getPriority() {
        assertThat(stepAuthorizedKeyIdVerification.getPriority(), is(Verification.Priority.STEP_AUTHORIZED_KEYID));
    }

    @Test
    void verifyWithCorrectKeyIdShouldReturnValidResponse() {
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
        when(linkMetaBlock.getLink().getStepName()).thenReturn(STEP_NAME);
        when(context.getLayoutMetaBlock().getLayout().getLayoutSegments().get(0).getSteps()).thenReturn(Collections.singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(Collections.singletonList("keyId"));
        when(context.getStepBySegmentNameAndStepName(eq(SEGMENT_NAME), eq(STEP_NAME))).thenReturn(step);
        when(linkMetaBlock.getSignature().getKeyId()).thenReturn("keyId");
        when(linkMetaBlock.getLink().getLayoutSegmentName()).thenReturn(SEGMENT_NAME);
        VerificationRunResult result = stepAuthorizedKeyIdVerification.verify(context);
        verify(context, times(0)).removeLinkMetaBlocks(listArgumentCaptor.capture());
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithCorrectIncorrectKeyIdShouldReturnInValidResponse() {
        when(context.getLinkMetaBlocks()).thenReturn(Collections.singletonList(linkMetaBlock));
        when(context.getLayoutMetaBlock().getLayout().getLayoutSegments().get(0).getSteps()).thenReturn(Collections.singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(Collections.singletonList("keyId"));
        when(linkMetaBlock.getSignature().getKeyId()).thenReturn("unTrustedKeyId");
        VerificationRunResult result = stepAuthorizedKeyIdVerification.verify(context);
        verify(context).removeLinkMetaBlocks(listArgumentCaptor.capture());
        assertThat(listArgumentCaptor.getValue(), hasSize(1));
        assertThat(result.isRunIsValid(), is(true));
    }
}
