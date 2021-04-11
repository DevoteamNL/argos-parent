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

import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.verification.RulesVerification;
import com.argosnotary.argos.service.domain.verification.VerificationContext;
import com.argosnotary.argos.service.domain.verification.rules.DisallowRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.MatchRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerificationContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.EXPECTED_END_PRODUCTS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpectedEndProductsVerificationTest {

    public static final String STEP_NAME = "stepName";
    private static final String DESTINATION_STEP_NAME1 = "destinationStepName1";
    private static final String DESTINATION_STEP_NAME2 = "destinationStepName2";
    private static final String SRC_PATH_PREFIX = "src";
    private static final String PATTERN1 = "cool1.jar";
    private static final String PATTERN2 = "cool2.jar";
    private static final String PATTERN3 = "*.jar";
    private static final String PATTERN4 = "*.sfx";
    private static final String ART1 = "cool1.jar";
    private static final String ART2 = "cool2.jar";
    private static final String ART3 = "cool1.sfx";
    private static final String ART4 = "cool2.sfx";
    private static final String NO_MATCH1 = "no-match";
    private static final String HASH1 = "hash1";
    private static final String HASH2 = "hash2";
    
    private ExpectedEndProductsVerification verification;

    @Mock
    private RuleVerificationContext<MatchRule> context;
    
    @Mock
    private ArtifactsVerificationContext artifactsContext;
    
    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    private Map<String, Map<String, Set<Link>>> linksMap;

    private Artifact artifact1 = new Artifact(ART1, HASH1);
    private Artifact artifact2 = new Artifact(ART2, HASH2);
    private Artifact artifact3 = new Artifact(ART3, HASH1);
    private Artifact artifact4 = new Artifact(ART4, HASH2);
    
    private Step step1 = Step.builder().name(DESTINATION_STEP_NAME1).build();
    private Step step2 = Step.builder().name(DESTINATION_STEP_NAME2).build();
        
    private VerificationContext verificationContext;
    
    private List<LinkMetaBlock> linkMetaBlocks;
    private LinkMetaBlock linkMetaBlock1;
    private LinkMetaBlock linkMetaBlock2;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;
    
    private MatchRule matchRule1;
    private MatchRule matchRule2;
    
    private RuleVerification matchRuleVerification = new MatchRuleVerification();

    @Captor
    private ArgumentCaptor<RuleVerificationContext<?>> ruleVerificationContextArgumentCaptor;
    
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        
        
    }

    @BeforeEach
    void setUp() {
        verification = new ExpectedEndProductsVerification(List.of(matchRuleVerification));
        verification.init();
        
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(EXPECTED_END_PRODUCTS));
    }
    
    @Test
    void verifyToVerifyProductsMatchesOneRule() {
        matchRule1 = new MatchRule("cool1.jar", null, ArtifactType.PRODUCTS, null, DESTINATION_STEP_NAME1);
        layout = Layout.builder().expectedEndProducts(List.of(matchRule1)).steps(List.of(step1)).build();
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .products(List.of(artifact1))
                        .stepName(DESTINATION_STEP_NAME1).build()).build()));
        verificationContext = VerificationContext
                .builder()
                .linkMetaBlocks(linkMetaBlocks)
                .layoutMetaBlock(layoutMetaBlock)
                .productsToVerify(Set.of(artifact1))
                .build();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));
    }
    
    @Test
    void verifyToVerifyProductsMatchesSeveralRules() {
        matchRule1 = new MatchRule(PATTERN1, null, ArtifactType.PRODUCTS, null, DESTINATION_STEP_NAME1);
        matchRule2 = new MatchRule(PATTERN2, null, ArtifactType.PRODUCTS, null, DESTINATION_STEP_NAME1);
        layout = Layout.builder().expectedEndProducts(List.of(matchRule1, matchRule2)).steps(List.of(step1)).build();
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .products(List.of(artifact1, artifact2))
                        .stepName(DESTINATION_STEP_NAME1).build()).build()));
        verificationContext = VerificationContext
                .builder()
                .linkMetaBlocks(linkMetaBlocks)
                .layoutMetaBlock(layoutMetaBlock)
                .productsToVerify(Set.of(artifact1))
                .build();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));
    }
    
    @Test
    void verifyToVerifyProductsSubsetOfMatched() {
        matchRule1 = new MatchRule(PATTERN3, null, ArtifactType.PRODUCTS, null, DESTINATION_STEP_NAME1);
        layout = Layout.builder().expectedEndProducts(List.of(matchRule1)).steps(List.of(step1)).build();
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .products(List.of(artifact1, artifact2))
                        .stepName(DESTINATION_STEP_NAME1).build()).build()));
        verificationContext = VerificationContext
                .builder()
                .linkMetaBlocks(linkMetaBlocks)
                .layoutMetaBlock(layoutMetaBlock)
                .productsToVerify(Set.of(artifact1))
                .build();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));
    }
    
    @Test
    void verifyMatchedSubsetOfToVerifyProducts() {
        matchRule1 = new MatchRule(PATTERN1, null, ArtifactType.PRODUCTS, null, DESTINATION_STEP_NAME1);
        layout = Layout.builder().expectedEndProducts(List.of(matchRule1)).steps(List.of(step1)).build();
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .products(List.of(artifact1, artifact2))
                        .stepName(DESTINATION_STEP_NAME1).build()).build()));
        verificationContext = VerificationContext
                .builder()
                .linkMetaBlocks(linkMetaBlocks)
                .layoutMetaBlock(layoutMetaBlock)
                .productsToVerify(Set.of(artifact1, artifact2))
                .build();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));

    }
    
    @Test
    void verifyToVerifyProductsMatchesSeveralRules2Steps() {
        matchRule1 = new MatchRule(PATTERN3, null, ArtifactType.PRODUCTS, null, DESTINATION_STEP_NAME1);
        matchRule2 = new MatchRule(PATTERN4, null, ArtifactType.MATERIALS, null, DESTINATION_STEP_NAME2);
        layout = Layout.builder().expectedEndProducts(List.of(matchRule1, matchRule2)).steps(List.of(step1, step2)).build();
        layoutMetaBlock = LayoutMetaBlock.builder().layout(layout).build();
        linkMetaBlock1 = LinkMetaBlock
                .builder()
                .link(Link.builder()
                        .products(List.of(artifact1, artifact2))
                        .stepName(DESTINATION_STEP_NAME1)
                        .build())
                .build();
        linkMetaBlock2 = LinkMetaBlock
                .builder()
                .link(Link.builder()
                        .materials(List.of(artifact3, artifact4))
                        .stepName(DESTINATION_STEP_NAME2)
                        .build())
                .build();
        linkMetaBlocks = new ArrayList<>(List.of(linkMetaBlock1,linkMetaBlock2));
        verificationContext = VerificationContext
                .builder()
                .linkMetaBlocks(linkMetaBlocks)
                .layoutMetaBlock(layoutMetaBlock)
                .productsToVerify(Set.of(artifact1, artifact2, artifact3, artifact4))
                .build();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));
    }
}
