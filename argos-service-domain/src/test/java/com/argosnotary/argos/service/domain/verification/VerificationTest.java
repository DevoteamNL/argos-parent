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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.verification.LayoutAuthorizedKeyIdVerification;
import com.argosnotary.argos.service.domain.verification.LayoutMetaBlockSignatureVerification;
import com.argosnotary.argos.service.domain.verification.LinkMetaBlockSignatureVerification;
import com.argosnotary.argos.service.domain.verification.RequiredNumberOfLinksVerification;
import com.argosnotary.argos.service.domain.verification.RulesVerification;
import com.argosnotary.argos.service.domain.verification.StepAuthorizedKeyIdVerification;
import com.argosnotary.argos.service.domain.verification.Verification;
import com.argosnotary.argos.service.domain.verification.VerificationContextsProvider;
import com.argosnotary.argos.service.domain.verification.VerificationProvider;
import com.argosnotary.argos.service.domain.verification.rules.AllowRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.CreateRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.DeleteRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.DisallowRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.MatchRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.ModifyRuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.RequireRuleVerification;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.crypto.signing.Signer;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.Step.StepBuilder;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.Link.LinkBuilder;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

@ExtendWith(MockitoExtension.class)
class VerificationTest {
    
    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    private List<Verification> verifications;
    
    private RulesVerification rulesVerification;

    private VerificationContextsProvider verificationContextsProvider;

    private VerificationProvider verificationProvider;
    
    private char[] passphrase = "test".toCharArray();
    
    private String SUPPLYCHAIN_NAME = "supplyChain";
    
    private String SUPPLYCHAIN_ID = "supplyChainId";
    
    private String SEGMENT1 = "segment1";
    private String SEGMENT2 = "segment2";
    
    private char[] PASSWORD = "password".toCharArray();
    
    private KeyPair bobKey;
    private KeyPair aliceKey;
    private KeyPair carlKey;
    
    private PublicKey bobPublicKey;
    private PublicKey alicePublicKey;
    private PublicKey carlPublicKey;
    
    StepBuilder segment1Step1Builder;
    LinkBuilder segment1Step1LinkBuilder;
    StepBuilder segment2Step1Builder;
    LinkBuilder segment2Step1LinkBuilder;
    

    
    @BeforeEach
    void setup() throws Exception {
    	bobKey = KeyPair.createKeyPair(PASSWORD);
        aliceKey = KeyPair.createKeyPair(PASSWORD);
        carlKey = KeyPair.createKeyPair(PASSWORD);
        bobPublicKey = new PublicKey(bobKey.getKeyId(), bobKey.getPublicKey());
        alicePublicKey = new PublicKey(aliceKey.getKeyId(), aliceKey.getPublicKey());
        carlPublicKey = new PublicKey(carlKey.getKeyId(), carlKey.getPublicKey());
        rulesVerification = new RulesVerification(List.of(
                new AllowRuleVerification(), 
                new CreateRuleVerification(),
                new DeleteRuleVerification(),
                new DisallowRuleVerification(),
                new MatchRuleVerification(),
                new ModifyRuleVerification(),
                new RequireRuleVerification()));
        rulesVerification.init();
        verifications = Arrays.asList(
                new LayoutAuthorizedKeyIdVerification(),
                new LayoutMetaBlockSignatureVerification(),
                new LinkMetaBlockSignatureVerification(),
                new RequiredNumberOfLinksVerification(),
                rulesVerification,
                new StepAuthorizedKeyIdVerification()
                );
        verificationContextsProvider = new VerificationContextsProvider(linkMetaBlockRepository, List.of(
                new AllowRuleVerification(), 
                new CreateRuleVerification(),
                new DeleteRuleVerification(),
                new DisallowRuleVerification(),
                new MatchRuleVerification(),
                new ModifyRuleVerification(),
                new RequireRuleVerification()));
        verificationContextsProvider.init();
        verifications.sort(Comparator.comparing(Verification::getPriority));
        verificationProvider = new VerificationProvider(verifications, verificationContextsProvider);
        verificationProvider.init();
    
        segment1Step1LinkBuilder = Link.builder()
                .layoutSegmentName("segment1")
                .stepName("step1");
        segment1Step1Builder = Step.builder()
                .name("step1");
        segment2Step1LinkBuilder = Link.builder()
                .layoutSegmentName("segment2")
                .stepName("step1");
        segment2Step1Builder = Step.builder()
                .name("step1");
    }

    @Test
    void happyFlow() throws JsonParseException, JsonMappingException, IOException, GeneralSecurityException {        
        Artifact artifact1 = new Artifact("file1", "hash1");
        
        Step step1 = segment1Step1Builder
                .authorizedKeyIds(List.of(aliceKey.getKeyId()))
                .expectedMaterials(List.of(new Rule(RuleType.ALLOW, "**")))
                .expectedProducts(List.of(new Rule(RuleType.ALLOW, "**")))
                .requiredNumberOfLinks(1).build();
        Layout layout = Layout.builder()
                .authorizedKeyIds(List.of(bobKey.getKeyId()))
                .keys(List.of(bobPublicKey,alicePublicKey))
                .expectedEndProducts(List.of(MatchRule.builder()
                        .destinationSegmentName("segment1")
                        .destinationStepName("step1")
                        .destinationType(ArtifactType.PRODUCTS)
                        .pattern("**").build()))
                .layoutSegments(List.of(LayoutSegment.builder()
                        .name("segment1").steps(List.of(step1)).build()))
                .build();
        Signature signature = Signer.sign(bobKey, PASSWORD, new JsonSigningSerializer().serialize(layout));
        LayoutMetaBlock layoutMetaBlock  = LayoutMetaBlock.builder().supplyChainId(SUPPLYCHAIN_ID).layout(layout).signatures(List.of(signature)).build();
        
        Link segment1Step1Link = segment1Step1LinkBuilder
                .runId("runId1")
                .materials(List.of(artifact1))
                .products(List.of(artifact1))
                .build();
        
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment1Step1Link));
        LinkMetaBlock alicesStep1Block  = LinkMetaBlock.builder().link(segment1Step1Link).signature(signature).build();
        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes.put(ArtifactType.PRODUCTS, Set.of(artifact1));
        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
                SUPPLYCHAIN_ID,
                SEGMENT1,
                step1.getName(),
                artifactTypeHashes)).thenReturn(List.of(alicesStep1Block));

        when(linkMetaBlockRepository.findByRunId(SUPPLYCHAIN_ID, SEGMENT1, "runId1", Set.of("step1"))).thenReturn(List.of());
        
        VerificationRunResult result = verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact1));
        assertTrue(result.isRunIsValid());        
    }
    
    @Test
    void allRules() throws JsonParseException, JsonMappingException, IOException, GeneralSecurityException {        
        Artifact artifact1 = new Artifact("file1", "hash1");
        Artifact artifact21 = new Artifact("file2", "hash21");
        Artifact artifact22 = new Artifact("file2", "hash22");
        Artifact artifact3 = new Artifact("file3", "hash3");
        Artifact artifact4 = new Artifact("file4", "hash4");
        Artifact artifact5 = new Artifact("file5", "hash5");
        
        Step step1 = segment1Step1Builder
                .authorizedKeyIds(List.of(aliceKey.getKeyId()))
                .expectedMaterials(List.of(
                        new Rule(RuleType.REQUIRE, "f*3"),
                        new Rule(RuleType.DELETE, "file4"),
                        new Rule(RuleType.MODIFY, "file2*"),
                        new Rule(RuleType.ALLOW, "*4"),
                        new Rule(RuleType.ALLOW, "*5"),
                        new Rule(RuleType.DISALLOW, "**")
                        ))
                .expectedProducts(List.of(
                        new Rule(RuleType.CREATE, "file1"),
                        MatchRule.builder()
                            .pattern("*5")
                            .destinationSegmentName(SEGMENT1)
                            .destinationStepName("step1")
                            .destinationType(ArtifactType.MATERIALS).build(),
                        new Rule(RuleType.ALLOW, "**")))
                .requiredNumberOfLinks(1).build();
        
        Layout layout = Layout.builder()
                .authorizedKeyIds(List.of(bobKey.getKeyId()))
                .keys(List.of(bobPublicKey,alicePublicKey))
                .expectedEndProducts(List.of(MatchRule.builder()
                        .destinationSegmentName("segment1")
                        .destinationStepName("step1")
                        .destinationType(ArtifactType.PRODUCTS)
                        .pattern("*5").build()))
                .layoutSegments(List.of(LayoutSegment.builder()
                        .name("segment1").steps(List.of(step1)).build()))
                .build();
        
        Signature signature = Signer.sign(bobKey, PASSWORD, new JsonSigningSerializer().serialize(layout));
        LayoutMetaBlock layoutMetaBlock  = LayoutMetaBlock.builder().supplyChainId(SUPPLYCHAIN_ID).layout(layout).signatures(List.of(signature)).build();
        
        Link segment1Step1Link = segment1Step1LinkBuilder
                .runId("runId1")
                .materials(List.of(artifact21, artifact3, artifact4, artifact5))
                .products(List.of(artifact1, artifact22, artifact5))
                .build();
        
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment1Step1Link));
        LinkMetaBlock alicesStep1Block  = LinkMetaBlock.builder().link(segment1Step1Link).signature(signature).build();

        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes.put(ArtifactType.PRODUCTS, Set.of(artifact5));

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
                SUPPLYCHAIN_ID,
                SEGMENT1,
                step1.getName(),
                artifactTypeHashes)).thenReturn(List.of(alicesStep1Block));

        when(linkMetaBlockRepository.findByRunId(SUPPLYCHAIN_ID, SEGMENT1, "runId1", Set.of("step1"))).thenReturn(List.of());
        
        VerificationRunResult result = verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact5));
        assertTrue(result.isRunIsValid());        
    }
    
    @Test
    void matchRuleWithDirs() throws JsonParseException, JsonMappingException, IOException, GeneralSecurityException {
        Artifact artifact1Dir1 = new Artifact("dir1/file1", "hash1");
        Artifact artifact1Dir2 = new Artifact("dir2/file1", "hash1");
        Artifact artifact1 = new Artifact("dir1/file1", "hash1");
        
        Step segment1Step1 = segment1Step1Builder
                .authorizedKeyIds(List.of(aliceKey.getKeyId()))
                .expectedMaterials(List.of(
                        MatchRule.builder()
                            .pattern("file1")
                            .sourcePathPrefix("dir1")
                            .destinationPathPrefix("dir2")
                            .destinationSegmentName(SEGMENT2)
                            .destinationStepName("step1")
                            .destinationType(ArtifactType.PRODUCTS)
                            .build()
                        ))
                .expectedProducts(List.of(
                        new Rule(RuleType.ALLOW, "**")))
                .requiredNumberOfLinks(1).build();
        
        Step segment2Step1 = segment2Step1Builder
                .authorizedKeyIds(List.of(aliceKey.getKeyId()))
                .expectedMaterials(List.of(
                        new Rule(RuleType.ALLOW, "**")
                        ))
                .expectedProducts(List.of(
                        new Rule(RuleType.ALLOW, "dir2/file1")))
                .requiredNumberOfLinks(1).build();
        
        Layout layout = Layout.builder()
                .authorizedKeyIds(List.of(bobKey.getKeyId()))
                .keys(List.of(bobPublicKey,alicePublicKey))
                .expectedEndProducts(List.of(MatchRule.builder()
                        .destinationSegmentName(SEGMENT1)
                        .destinationStepName("step1")
                        .destinationType(ArtifactType.PRODUCTS)
                        .pattern("**").build()))
                .layoutSegments(List.of(
                        LayoutSegment.builder()
                            .name(SEGMENT1)
                            .steps(List.of(segment1Step1))
                            .build(),
                        LayoutSegment.builder()
                            .name(SEGMENT2)
                            .steps(List.of(segment2Step1))
                            .build()))
                .build();
        
        Signature signature = Signer.sign(bobKey, PASSWORD, new JsonSigningSerializer().serialize(layout));
        LayoutMetaBlock layoutMetaBlock  = LayoutMetaBlock.builder().supplyChainId(SUPPLYCHAIN_ID).layout(layout).signatures(List.of(signature)).build();
        
        Link segment1Step1Link = segment1Step1LinkBuilder
                .runId("runId1")
                .materials(List.of(artifact1Dir1))
                .products(List.of(artifact1Dir1))
                .build();
        
        Link segment2Step1Link = segment2Step1LinkBuilder
                .runId("runId2")
                .materials(List.of(artifact1Dir2))
                .products(List.of(artifact1Dir2))
                .build();
        
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment1Step1Link));
        LinkMetaBlock alicesSegment1Step1Block  = LinkMetaBlock.builder().link(segment1Step1Link).signature(signature).build();
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment2Step1Link));
        LinkMetaBlock alicesSegment2Step1Block  = LinkMetaBlock.builder().link(segment2Step1Link).signature(signature).build();

        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes1 = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes1.put(ArtifactType.PRODUCTS, Set.of(artifact1Dir1));
        
        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes2 = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes2.put(ArtifactType.PRODUCTS, Set.of(artifact1Dir2));

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
                SUPPLYCHAIN_ID,
                SEGMENT1,
                segment1Step1.getName(),
                artifactTypeHashes1)).thenReturn(List.of(alicesSegment1Step1Block));

        when(linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
                SUPPLYCHAIN_ID,
                SEGMENT2,
                segment2Step1.getName(),
                artifactTypeHashes2)).thenReturn(List.of(alicesSegment2Step1Block));

        when(linkMetaBlockRepository.findByRunId(SUPPLYCHAIN_ID, SEGMENT1, "runId1", Set.of("step1"))).thenReturn(List.of());
        when(linkMetaBlockRepository.findByRunId(SUPPLYCHAIN_ID, SEGMENT2, "runId2", Set.of("step1"))).thenReturn(List.of());
        
        VerificationRunResult result = verificationProvider.verifyRun(layoutMetaBlock, List.of(artifact1));
        assertTrue(result.isRunIsValid());        
    }

}
