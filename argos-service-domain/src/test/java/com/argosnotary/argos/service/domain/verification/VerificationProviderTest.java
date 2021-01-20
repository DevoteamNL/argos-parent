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

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationProviderTest {

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Artifact artifact;

    @Mock(name = "high")
    private Verification highPrio;

    @Mock(name = "low")
    private Verification lowPrio;

    @Mock
    private VerificationContext verificationContext;

    private List<Verification> verifications;

    @Mock
    private VerificationRunResult verificationRunResultLow;

    @Mock
    private VerificationRunResult verificationRunResultHigh;

    @Captor
    private ArgumentCaptor<VerificationContext> verificationContextArgumentCaptor;

    @Mock
    private VerificationContextsProvider verificationContextsProvider;

    private VerificationProvider verificationProvider;

    @BeforeEach
    void setup() {
        verifications = new ArrayList<>();
        verificationProvider = new VerificationProvider(verifications, verificationContextsProvider);
    }

    @Test
    void verifyShouldProduceVerificationRunResult() {
        setupMocking();
        when(lowPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultLow);
        when(highPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultHigh);
        when(verificationRunResultLow.isRunIsValid()).thenReturn(true);
        when(verificationRunResultHigh.isRunIsValid()).thenReturn(true);
        assertThat(verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact)).isRunIsValid(), is(true));
        verify(lowPrio).verify(verificationContextArgumentCaptor.capture());
    }

    @Test
    void verifyShouldProduceFalseVerificationRunResult() {
        setupMocking();
        when(verificationRunResultHigh.isRunIsValid()).thenReturn(false);
        when(highPrio.verify(any(VerificationContext.class))).thenReturn(verificationRunResultHigh);
        assertThat(verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact)).isRunIsValid(), is(false));
    }

    private void setupMocking() {
        when(lowPrio.getPriority()).thenReturn(Verification.Priority.RULES);
        when(highPrio.getPriority()).thenReturn(Verification.Priority.LAYOUT_METABLOCK_SIGNATURE);
        when(verificationContextsProvider.createPossibleVerificationContexts(any(), any())).thenReturn(singletonList(verificationContext));
        verifications.add(lowPrio);
        verifications.add(highPrio);
        verificationProvider.init();
    }

}