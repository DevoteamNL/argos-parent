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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.argosnotary.argos.domain.crypto.Signature;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutAuthorizedKeyIdVerificationTest {

    private static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    private LayoutAuthorizedKeyIdVerification layoutAuthorizedKeyIdVerification;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VerificationContext context;

    @Mock
    private Signature signature;

    @BeforeEach
    void setUp() {
        layoutAuthorizedKeyIdVerification = new LayoutAuthorizedKeyIdVerification();
    }

    @Test
    void getPriority() {
        assertThat(layoutAuthorizedKeyIdVerification.getPriority(), is(Verification.Priority.LAYOUT_AUTHORIZED_KEYID));
    }

    @Test
    void verifyWithCorrectKeyIdShouldReturnValidResponse() {
        when(signature.getKeyId()).thenReturn(KEY_1);
        when(context.getLayoutMetaBlock().getSignatures()).thenReturn(singletonList(signature));
        when(context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds()).thenReturn(singletonList(KEY_1));
        VerificationRunResult result = layoutAuthorizedKeyIdVerification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyWithInCorrectKeyIdShouldReturnInValidResponse() {
        when(signature.getKeyId()).thenReturn(KEY_1);
        when(context.getLayoutMetaBlock().getSignatures()).thenReturn(singletonList(signature));
        when(context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds()).thenReturn(singletonList(KEY_2));
        VerificationRunResult result = layoutAuthorizedKeyIdVerification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }
}