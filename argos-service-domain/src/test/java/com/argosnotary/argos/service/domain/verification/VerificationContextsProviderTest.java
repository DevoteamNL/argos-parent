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
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.verification.rules.AllowRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.CreateRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.DeleteRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.DisallowRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.MatchRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.ModifyRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.RequireRuleVerification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationContextsProviderTest {

    private static final String STEP_NAME_1 = "stepName1";
    private static final String STEP_NAME_2 = "stepName2";
    private static final String STEP_NAME_3 = "stepName3";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;

    @Mock
    private Step step1;

    @Mock
    private Step step2;

    @Mock
    private Step step3;

    private List<Artifact> artifacts;
    private List<Artifact> artifacts2;

    private List<MatchRule> matchRule;

    private List<Rule> matchRulesForProductsStep1;

    private LinkMetaBlock linkMetaBlockFromInput;

    private LinkMetaBlock linkMetaBlockFromRunId1_1;

    private LinkMetaBlock linkMetaBlockFromRunId1_2;

    private VerificationContextsProvider verificationContextsProvider;

    private LinkMetaBlock linkMetaBlockFromInput2;

    private LinkMetaBlock linkMetaBlockFromMatchRuleSegment2_1;

    private LinkMetaBlock linkMetaBlockFromMatchRuleSegment2_2;

    private LinkMetaBlock linkMetaBlockFromMatchRuleSegment3;

    @BeforeEach
    void setup() {
        createMatchRules();
        createArtifacts();

        linkMetaBlockFromInput = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_1)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromInput2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_1)
                        .materials(artifacts)
                        .products(artifacts2)
                        .build()
                ).build();

        linkMetaBlockFromMatchRuleSegment2_1 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_2)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromMatchRuleSegment3 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_3)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromRunId1_1 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_2)
                        .materials(artifacts)
                        .products(artifacts)
                        .build()
                ).build();

        linkMetaBlockFromRunId1_2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_2)
                        .materials(artifacts)
                        .products(artifacts2)
                        .build()
                ).build();

        verificationContextsProvider = new VerificationContextsProvider(linkMetaBlockRepository, List.of(
                new AllowRuleVerification(), 
                new CreateRuleVerification(),
                new DeleteRuleVerification(),
                new DisallowRuleVerification(),
                new MatchRuleVerification(),
                new ModifyRuleVerification(),
                new RequireRuleVerification()));
        verificationContextsProvider.init();
    }

    private void createArtifacts() {
        Artifact artifact1 = Artifact
                .builder()
                .hash("hash1")
                .uri("path/artifact.jar")
                .build();
        Artifact artifact2 = Artifact
            .builder()
            .hash("hash2")
            .uri("path/artifact2.jar")
            .build();
        artifacts = List.of(artifact1);
        artifacts2 = List.of(artifact1, artifact2);
    }

    private void createMatchRules() {
        MatchRule matchFilterProduct = MatchRule.builder()
                .destinationType(ArtifactType.PRODUCTS)
                .destinationStepName(STEP_NAME_1)
                .pattern("**/*.jar")
                .build();

        MatchRule matchFilterMaterials = MatchRule.builder()
                .destinationType(ArtifactType.MATERIALS)
                .destinationStepName(STEP_NAME_1)
                .pattern("**/*.jar")
                .build();

        matchRule = List.of(matchFilterProduct, matchFilterMaterials);
    }

    private void createMatchRuleMaterials() {

        MatchRule matchFilterMaterials = MatchRule.builder()
                .destinationType(ArtifactType.MATERIALS)
                .destinationStepName(STEP_NAME_1)
                .pattern("**/*.jar")
                .build();

        matchRule = List.of(matchFilterMaterials);
    }

    @Test
    void createPossibleVerificationContextsWithMultipleStepsAndMultipleEqualLinkSets() {
        setupMocksForSingleLink();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, new HashSet<>(artifacts));
        assertThat(verificationContexts, hasSize(1));
    }

    @Test
    void createPossibleVerificationContextsSingleStepAndMultipleEqualLinkSets() {
        setupMocksForSingleStep();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, new HashSet<>(artifacts));
        assertThat(verificationContexts, hasSize(2));
    }


    @Test
    void createPossibleVerificationContextsWithNonMatchingArtifacts() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID))
                .thenReturn(List.of());
        
        Artifact wrongArtifact = Artifact.builder().uri("/wrong.exe").hash("hash").build();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, Set.of(wrongArtifact));
        assertThat(verificationContexts, hasSize(0));
    }

    @Test
    void createPossibleVerificationContextsWithMatchinMaterialArtifacts() {
        setupMocksForSingleLink();
        createMatchRuleMaterials();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, new HashSet<>(artifacts));
        assertThat(verificationContexts, hasSize(1));
    }

    @Test
    void createPossibleVerificationContextsWithMultipleSegmentsShouldReturnOneVerificationContext() {
        setupMocksForMultipleSteps();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, new HashSet<>(artifacts));
        assertThat(verificationContexts, hasSize(1));
        VerificationContext verificationContext = verificationContexts.iterator().next();
        assertThat(verificationContext.getLinkMetaBlocks(), hasSize(3));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromInput), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment2_1), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment3), is(true));
    }

    @Test
        // with hop mean that from segment one upstream segment 2 is resolved and then from segment 2 segment 3 is resolved
    void createPossibleVerificationContextsWithMultipleSegmentsWithHopShouldReturnOneVerificationContext() {

        MatchRule matchRule4StepOne2StepTwo = MatchRule.builder()
                .destinationStepName(STEP_NAME_2).pattern("**/*.jar")
                .destinationType(ArtifactType.PRODUCTS)
                .build();

        MatchRule matchRule4StepTwo2StepThree = MatchRule.builder()
                .destinationStepName(STEP_NAME_3).pattern("**/*.jar")
                .destinationType(ArtifactType.PRODUCTS)
                .build();

        matchRulesForProductsStep1 = List.of(matchRule4StepOne2StepTwo);
        List<Rule> matchRulesForProductsStep2 = List.of(matchRule4StepTwo2StepThree);
        
        Step step1 = Step.builder().name(STEP_NAME_1).expectedProducts(matchRulesForProductsStep1).build();
        Step step2 = Step.builder().name(STEP_NAME_2).expectedProducts(matchRulesForProductsStep2).build();
        Step step3 = Step.builder().name(STEP_NAME_3).build();
        
        Layout layout = Layout.builder().expectedEndProducts(matchRule).steps(List.of(step1, step2, step3)).build();        
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder().supplyChainId(SUPPLY_CHAIN_ID).layout(layout).build();
        
        Map<String, EnumMap<ArtifactType, Set<Artifact>>> stepMap1 = new HashMap<>();
        
        
        

        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID))
                .thenReturn(List.of(linkMetaBlockFromInput, linkMetaBlockFromMatchRuleSegment2_1, linkMetaBlockFromMatchRuleSegment3));

        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, new HashSet<>(artifacts));
        assertThat(verificationContexts, hasSize(1));
        VerificationContext verificationContext = verificationContexts.iterator().next();
        assertThat(verificationContext.getLinkMetaBlocks(), hasSize(3));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromInput), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment2_1), is(true));
        assertThat(verificationContext.getLinkMetaBlocks().contains(linkMetaBlockFromMatchRuleSegment3), is(true));
    }

    @Test
    void createPossibleVerificationContextsWithMultipleSegmentsWithHopShouldReturnFourVerificationContexts() {
        setupMocksWithHopMultipleSets();
        List<VerificationContext> verificationContexts = verificationContextsProvider.createPossibleVerificationContexts(layoutMetaBlock, new HashSet<>(artifacts));
        assertThat(verificationContexts, hasSize(2));
    }
    
    @Test
    void permutateAndAddLinkMetaBlocksTest() {
        Artifact artifact11 = new Artifact("file11", "hash111");
        Artifact artifact12 = new Artifact("file12", "hash112");
        Artifact artifact21 = new Artifact("file21", "hash121");
        Artifact artifact22 = new Artifact("file22", "hash122");
        Artifact artifact31 = new Artifact("file31", "hash211");
        Artifact artifact32 = new Artifact("file32", "hash212");

        Link link11 = Link.builder().stepName("step1").materials(List.of(artifact11)).build();
        Link link12 = Link.builder().stepName("step1").materials(List.of(artifact12)).build();
        Link link21 = Link.builder().stepName("step2").materials(List.of(artifact21)).build();
        Link link22 = Link.builder().stepName("step2").materials(List.of(artifact22)).build();
        LinkMetaBlock block11 = LinkMetaBlock.builder().link(link11).build();
        LinkMetaBlock block12 = LinkMetaBlock.builder().link(link12).build();
        LinkMetaBlock block21 = LinkMetaBlock.builder().link(link21).build();
        LinkMetaBlock block22 = LinkMetaBlock.builder().link(link22).build();
        Set<Set<LinkMetaBlock>> expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block11, block21));
        expectedSets.add(Set.of(block11, block22));
        expectedSets.add(Set.of(block12, block21));
        expectedSets.add(Set.of(block12, block22));

        Set<Set<LinkMetaBlock>> actualSets = VerificationContextsProvider.permutateOnSteps(Set.of(block11, block12, block21, block22));
        
        assertThat(actualSets, is(expectedSets));

        Link link3 = Link.builder().stepName("step3").materials(List.of(artifact31)).build();
        Signature sig1 = Signature.builder().keyId("keyId1").build();
        Signature sig2 = Signature.builder().keyId("keyId2").build();
        LinkMetaBlock block31 = LinkMetaBlock.builder().link(link3).signature(sig1).build();
        LinkMetaBlock block32 = LinkMetaBlock.builder().link(link3).signature(sig2).build();
        
        expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block11, block21, block31, block32));
        expectedSets.add(Set.of(block12, block21, block31, block32));
        expectedSets.add(Set.of(block11, block22, block31, block32));
        expectedSets.add(Set.of(block12, block22, block31, block32));

        actualSets = VerificationContextsProvider.permutateOnSteps(
                Set.of(block11, block12, block21, block22, block31, block32));
        
        assertThat(actualSets, is(expectedSets));
    }
    
    @Test
    void permutateOnStepsAndWithMultipleLinks() {
        Artifact artifact111 = new Artifact("file111", "hash111");
        Artifact artifact112 = new Artifact("file112", "hash112");
        Artifact artifact121 = new Artifact("file121", "hash121");
        Artifact artifact122 = new Artifact("file122", "hash122");

        Link link111 = Link.builder().stepName("step11").materials(List.of(artifact111)).build();
        Link link112 = Link.builder().stepName("step11").materials(List.of(artifact112)).build();
        Link link121 = Link.builder().stepName("step12").materials(List.of(artifact121)).build();
        Link link122 = Link.builder().stepName("step12").materials(List.of(artifact122)).build();
        LinkMetaBlock block111 = LinkMetaBlock.builder().link(link111).build();
        LinkMetaBlock block112 = LinkMetaBlock.builder().link(link112).build();
        LinkMetaBlock block121 = LinkMetaBlock.builder().link(link121).build();
        LinkMetaBlock block122 = LinkMetaBlock.builder().link(link122).build();
        Set<Set<LinkMetaBlock>> expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block111));
        expectedSets.add(Set.of(block112));
        Set<Set<LinkMetaBlock>> actualSets = VerificationContextsProvider.permutateOnSteps(Set.of(block111, block112));
        
        assertThat(actualSets, is(expectedSets));
        
        expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block111, block121));
        expectedSets.add(Set.of(block112, block121));
        expectedSets.add(Set.of(block111, block122));
        expectedSets.add(Set.of(block112, block122));
        actualSets = VerificationContextsProvider.permutateOnSteps(Set.of(block111, block112, block121, block122));
        
        assertThat(actualSets, is(expectedSets));
    }


    private void setupMocksForSingleStep() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(linkMetaBlockRepository
                .findBySupplyChainId(SUPPLY_CHAIN_ID))
                .thenReturn(List.of(linkMetaBlockFromInput, linkMetaBlockFromInput2));
    }

    void setupMocksForSingleLink() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID))
                .thenReturn(List.of(linkMetaBlockFromInput));
    }

    void setupMocksForMultipleSteps() {
        MatchRule matchRule4StepOne2StepTwo = MatchRule.builder()
                .destinationStepName(STEP_NAME_2).pattern("**/*.jar")
                .destinationType(ArtifactType.PRODUCTS)
                .build();

        MatchRule matchRule4StepOne2StepThree = MatchRule.builder()
                .destinationStepName(STEP_NAME_3).pattern("**/*.jar")
                .destinationType(ArtifactType.PRODUCTS)
                .build();
        matchRulesForProductsStep1 = List.of(matchRule4StepOne2StepTwo, matchRule4StepOne2StepThree);
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID))
            .thenReturn(List.of(linkMetaBlockFromInput, linkMetaBlockFromMatchRuleSegment2_1, linkMetaBlockFromMatchRuleSegment3));
    }

    private void setupMocksWithHopMultipleSets() {

        linkMetaBlockFromMatchRuleSegment2_2 = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLY_CHAIN_ID)
                .link(Link.builder()
                        .stepName(STEP_NAME_2)
                        .materials(new ArrayList<>(artifacts))
                        .products(new ArrayList<>(artifacts2))
                        .build()
                ).build();

        MatchRule matchRule4StepOne2StepTwo = MatchRule.builder()
                .destinationStepName(STEP_NAME_2).pattern("**/*.jar")
                .destinationType(ArtifactType.PRODUCTS)
                .build();

        MatchRule matchRule4StepTwo2StepThree = MatchRule.builder()
                .destinationStepName(STEP_NAME_3).pattern("**/*.jar")
                .destinationType(ArtifactType.PRODUCTS)
                .build();

        matchRulesForProductsStep1 = List.of(matchRule4StepOne2StepTwo);
        List<Rule> matchRulesForProductsStep2 = List.of(matchRule4StepTwo2StepThree);

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);

        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID))
                .thenReturn(List.of(linkMetaBlockFromInput, linkMetaBlockFromMatchRuleSegment2_1, linkMetaBlockFromMatchRuleSegment2_2, linkMetaBlockFromMatchRuleSegment3));
    }

}