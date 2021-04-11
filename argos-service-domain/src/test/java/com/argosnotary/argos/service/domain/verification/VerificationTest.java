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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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
    
    private String SUPPLYCHAIN_ID = "supplyChainId";
    
    private char[] PASSWORD = "password".toCharArray();
    
    private KeyPair bobKey;
    private KeyPair aliceKey;
    private KeyPair carlKey;
    
    private PublicKey bobPublicKey;
    private PublicKey alicePublicKey;
    private PublicKey carlPublicKey;
    
    StepBuilder step1Builder;
    LinkBuilder step1LinkBuilder;
    StepBuilder step2Builder;
    LinkBuilder step2LinkBuilder;
    

    
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
    
        step1LinkBuilder = Link.builder()
                .stepName("step1");
        step1Builder = Step.builder()
                .name("step1");
        step2LinkBuilder = Link.builder()
                .stepName("step2");
        step2Builder = Step.builder()
                .name("step2");
    }

    @Test
    void happyFlow() throws JsonParseException, JsonMappingException, IOException, GeneralSecurityException {        
        Artifact artifact1 = new Artifact("file1", "hash1");
        
        Step step1 = step1Builder
                .authorizedKeyIds(List.of(aliceKey.getKeyId()))
                .expectedMaterials(List.of(new Rule(RuleType.ALLOW, "**")))
                .expectedProducts(List.of(new Rule(RuleType.ALLOW, "**")))
                .requiredNumberOfLinks(1).build();
        Layout layout = Layout.builder()
                .authorizedKeyIds(List.of(bobKey.getKeyId()))
                .keys(List.of(bobPublicKey,alicePublicKey))
                .expectedEndProducts(List.of(MatchRule.builder()
                        .destinationStepName("step1")
                        .destinationType(ArtifactType.PRODUCTS)
                        .pattern("**").build()))
                .steps(List.of(step1))
                .build();
        Signature signature = Signer.sign(bobKey, PASSWORD, new JsonSigningSerializer().serialize(layout));
        LayoutMetaBlock layoutMetaBlock  = LayoutMetaBlock.builder().supplyChainId(SUPPLYCHAIN_ID).layout(layout).signatures(List.of(signature)).build();
        
        Link step1Link = step1LinkBuilder
                .materials(List.of(artifact1))
                .products(List.of(artifact1))
                .build();
        
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(step1Link));
        LinkMetaBlock alicesStep1Block  = LinkMetaBlock.builder().link(step1Link).signature(signature).build();
        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes.put(ArtifactType.PRODUCTS, Set.of(artifact1));
        Map<String, EnumMap<ArtifactType, Set<Artifact>>> stepMap = new HashMap<>();
        stepMap.put(step1.getName(), artifactTypeHashes);
        
        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLYCHAIN_ID)).thenReturn(Arrays.asList(alicesStep1Block));

        VerificationRunResult result = verificationProvider.verifyRun(layoutMetaBlock, Set.of(artifact1));
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
        
        Step step1 = step1Builder
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
                            .destinationStepName("step1")
                            .destinationType(ArtifactType.MATERIALS).build(),
                        new Rule(RuleType.ALLOW, "**")))
                .requiredNumberOfLinks(1).build();
        
        Layout layout = Layout.builder()
                .authorizedKeyIds(List.of(bobKey.getKeyId()))
                .keys(List.of(bobPublicKey,alicePublicKey))
                .expectedEndProducts(List.of(MatchRule.builder()
                        .destinationStepName("step1")
                        .destinationType(ArtifactType.PRODUCTS)
                        .pattern("*5").build()))
                .steps(List.of(step1))
                .build();
        
        Signature signature = Signer.sign(bobKey, PASSWORD, new JsonSigningSerializer().serialize(layout));
        LayoutMetaBlock layoutMetaBlock  = LayoutMetaBlock.builder().supplyChainId(SUPPLYCHAIN_ID).layout(layout).signatures(List.of(signature)).build();
        
        Link segment1Step1Link = step1LinkBuilder
                .materials(List.of(artifact21, artifact3, artifact4, artifact5))
                .products(List.of(artifact1, artifact22, artifact5))
                .build();
        
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment1Step1Link));
        LinkMetaBlock alicesStep1Block  = LinkMetaBlock.builder().link(segment1Step1Link).signature(signature).build();

        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes.put(ArtifactType.PRODUCTS, Set.of(artifact5));
        
        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLYCHAIN_ID)).thenReturn(List.of(alicesStep1Block));

        VerificationRunResult result = verificationProvider.verifyRun(layoutMetaBlock, Set.of(artifact5));
        assertTrue(result.isRunIsValid());        
    }
    
    @Test
    void matchRuleWithDirs() throws JsonParseException, JsonMappingException, IOException, GeneralSecurityException {
        Artifact artifact1Dir1 = new Artifact("dir1/file1", "hash1");
        Artifact artifact1Dir2 = new Artifact("dir2/file1", "hash1");
        Artifact artifact1 = new Artifact("dir1/file1", "hash1");
        
        Step step1 = step1Builder
                .authorizedKeyIds(List.of(aliceKey.getKeyId()))
                .expectedMaterials(List.of(
                        MatchRule.builder()
                            .pattern("file1")
                            .sourcePathPrefix("dir1")
                            .destinationPathPrefix("dir2")
                            .destinationStepName("step2")
                            .destinationType(ArtifactType.PRODUCTS)
                            .build()
                        ))
                .expectedProducts(List.of(
                        new Rule(RuleType.ALLOW, "**")))
                .requiredNumberOfLinks(1).build();
        
        Step step2 = step2Builder
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
                        .destinationStepName("step1")
                        .destinationType(ArtifactType.PRODUCTS)
                        .pattern("**").build()))
                .steps(List.of(step1, step2))
                .build();
        
        Signature signature = Signer.sign(bobKey, PASSWORD, new JsonSigningSerializer().serialize(layout));
        LayoutMetaBlock layoutMetaBlock  = LayoutMetaBlock.builder().supplyChainId(SUPPLYCHAIN_ID).layout(layout).signatures(List.of(signature)).build();
        
        Link segment1Step1Link = step1LinkBuilder
                .materials(List.of(artifact1Dir1))
                .products(List.of(artifact1Dir1))
                .build();
        
        Link segment2Step1Link = step2LinkBuilder
                .products(List.of(artifact1Dir2))
                .build();
        
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment1Step1Link));
        LinkMetaBlock alicesStep1Block  = LinkMetaBlock.builder().link(segment1Step1Link).signature(signature).build();
        signature = Signer.sign(aliceKey, PASSWORD, new JsonSigningSerializer().serialize(segment2Step1Link));
        LinkMetaBlock alicesStep2Block  = LinkMetaBlock.builder().link(segment2Step1Link).signature(signature).build();

        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes1 = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes1.put(ArtifactType.PRODUCTS, Set.of(artifact1Dir1));
        
        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes2 = new EnumMap<>(ArtifactType.class);
        artifactTypeHashes2.put(ArtifactType.PRODUCTS, Set.of(artifact1Dir2));
        
        Map<String, EnumMap<ArtifactType, Set<Artifact>>> stepMap = new HashMap<>();
        stepMap.put(step1.getName(), artifactTypeHashes1);
        stepMap.put(step2.getName(), artifactTypeHashes2);

        when(linkMetaBlockRepository.findBySupplyChainId(SUPPLYCHAIN_ID)).thenReturn(List.of(alicesStep1Block, alicesStep2Block));
        
        VerificationRunResult result = verificationProvider.verifyRun(layoutMetaBlock, Set.of(artifact1));
        assertTrue(result.isRunIsValid());        
    }

}
