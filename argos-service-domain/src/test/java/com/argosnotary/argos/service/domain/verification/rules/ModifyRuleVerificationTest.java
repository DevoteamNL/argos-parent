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
package com.argosnotary.argos.service.domain.verification.rules;

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.service.domain.verification.ArtifactsVerificationContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModifyRuleVerificationTest {

    private static final String ARTIFACT_URI = "uri";
    private static final String PRODUCT_HASH = "productHash";
    private static final String MATERIAL_HASH = "materialHash";

    private ModifyRuleVerification verification;

    @Mock
    private RuleVerificationContext<? extends Rule> context;
    
    @Mock
    private ArtifactsVerificationContext artifactsContext;

    private Artifact artifact = new Artifact(ARTIFACT_URI, PRODUCT_HASH);
    private Artifact artifact2 = new Artifact("uri2", "hash2");
    private Artifact artifact3 = new Artifact("uri3", "hash3");
    private Artifact productArtifact = new Artifact(ARTIFACT_URI, PRODUCT_HASH);
    private Artifact materialArtifact = new Artifact(ARTIFACT_URI, MATERIAL_HASH);

    @BeforeEach
    void setUp() {
        verification = new ModifyRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.MODIFY));
    }

    @Test
    void verifyArtifactsHappyFlow() {

        when(context.getFilteredArtifacts()).thenReturn(Set.of(artifact));
        when(context.getProducts()).thenReturn(Set.of(artifact2,productArtifact));
        when(context.getMaterials()).thenReturn(Set.of(artifact3, materialArtifact));

        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifact));
    }

    @Test
    void verifyArtifactsNotModified() {

        when(context.getFilteredArtifacts()).thenReturn(Set.of(artifact));
        when(context.getProducts()).thenReturn(Set.of(artifact));
        when(context.getMaterials()).thenReturn(Set.of(artifact));

        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }

    @Test
    void verifyArtifactsNoProductMatch() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of());
        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of());
    }

    @Test
    void verifyArtifactsNoMaterialMatch() {

        when(context.getProducts()).thenReturn(Set.of(productArtifact));
        when(context.getMaterials()).thenReturn(Set.of());

        when(context.getFilteredArtifacts()).thenReturn(Set.of(productArtifact));

        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }
}
