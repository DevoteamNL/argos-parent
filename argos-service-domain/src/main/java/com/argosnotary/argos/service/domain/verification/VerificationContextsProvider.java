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

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationContextsProvider {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    private final List<RuleVerification> ruleVerificationList;

    private Map<RuleType, RuleVerification> rulesVerificationMap = new EnumMap<>(RuleType.class);

    @PostConstruct
    public void init() {
        ruleVerificationList.forEach(ruleVerification -> rulesVerificationMap.put(ruleVerification.getRuleType(), ruleVerification));
    }

    /**
     * Create a list of Verification contexts starting with the end products.
     * 
     * @param layoutMetaBlock
     * @param productsToVerify List of expected product artifacts
     * @return List of VerificationContexts
     */
    public List<VerificationContext> createPossibleVerificationContexts(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {
        
        // create context
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder()
                .supplyChainId(layoutMetaBlock.getSupplyChainId())
                .layout(layoutMetaBlock.getLayout())
                .productsToVerify(new HashSet<>(productsToVerify))
                .rulesVerificationMap(rulesVerificationMap)
                .build();
        try {
            context.init();
            processMatchRules(context);
        } catch(ArgosError exc) {
            log.error(exc.getMessage());
            return List.of();
        }

        log.info("processMatchRules resulted in: {} possible verificationContexts", context.getLinkMetaBlockSets().size());
        
        return context
                .getLinkMetaBlockSets()
                .stream()
                .map(linkSet -> VerificationContext
                        .builder()
                        .layoutMetaBlock(layoutMetaBlock)
                        .linkMetaBlocks(new ArrayList<>(linkSet)).build())
                .collect(Collectors.toList());
    }
    
    /*
     * Resolve all segments by processing all Match Rules pointing to that destination segments.
     * 
     * First a topological ordering was made of the directed graph of the matchrules 
     * defined in the steps in the layout.
     * 
     * The linkMetaBlocks of the first segment are discovered with the MatchRule's
     * in the expected end products of the Layout.
     *  
     * For the second phase the matchrules pointing to a segment are used to discover 
     * the linkMetaBlock for every next segment in the ordered list.
     * 
     * @param context Context to store the input and hold the result.
     */
    private void processMatchRules(VerificationContextsProviderContext context) {
        LayoutSegment segment = null;       
        // get next destination segment to process
        Optional<LayoutSegment> optionalSegment = context.getNextSegment();
        if (optionalSegment.isPresent()) {
            segment = optionalSegment.get();
            // first segment based on products to verify 
            // and the first item in the ordered list
            if (context.getResolvedSegments().isEmpty()) {
                // initialization
                Map<String, Map<MatchRule, Set<Artifact>>> stepMap = context.getFirstMatchRulesAndArtifacts();
                // check if all productsToVerify are consumed
                if (context.getProductsToVerify().isEmpty()) {
                    processSegment(context, segment, stepMap);
                } else {
                    log.info("Not all products to verify are consumed {} left.", context.getProductsToVerify().size());
                }
            } else {
                for (Set<LinkMetaBlock> linkMetaBlockSet: context.getLinkMetaBlockSets()) {
                    Map<String, Map<MatchRule, Set<Artifact>>> destStepMap = context.getMatchRulesAndArtifacts(segment, linkMetaBlockSet);
                    processSegment(context, segment, destStepMap);
                }
            }
            // check if all productsToVerify are consumed
            if (context.getProductsToVerify().isEmpty()) {
                context.getResolvedSegments().add(segment);
                processMatchRules(context);
            } else {
                log.info("Not all products to verify are consumed {} left.", context.getProductsToVerify().size());
            }
        }
    }
    
    private void processSegment(VerificationContextsProviderContext context, LayoutSegment segment, Map<String, Map<MatchRule, Set<Artifact>>> destStepMap) {
        Set<String> resolvedSteps = new HashSet<>();
        // get links of dest steps in segment
        Set<LinkMetaBlock> linkMetaBlocks = new HashSet<>(new HashSet<>());
        destStepMap.keySet().forEach(stepName -> {
            Set<LinkMetaBlock> foundBlocks = queryByArtifacts(context.getSupplyChainId(), segment.getName(), stepName, destStepMap);
            log.info("[{}] LinkMetaBlocks found for: supply chain id: [{}] segment: [{}] step name: [{}]", foundBlocks.size(), context.getSupplyChainId(), segment.getName(), stepName);
            linkMetaBlocks.addAll(queryByArtifacts(context.getSupplyChainId(), segment.getName(), stepName, destStepMap));
            resolvedSteps.add(stepName);
        });
        
        linkMetaBlocks.addAll(findMetaBlocksWithRunIds(linkMetaBlocks, context, segment, resolvedSteps));
        if (context.getLinkMetaBlockSets().isEmpty()) {
            context.getLinkMetaBlockSets().add(new HashSet<>());
        }
        
        context.setLinkMetaBlockSets(VerificationContextsProviderContext.permutateAndAddLinkMetaBlocks(linkMetaBlocks, context.getLinkMetaBlockSets()));
    }
    
    private Set<LinkMetaBlock> findMetaBlocksWithRunIds(Set<LinkMetaBlock> linkMetaBlocks, VerificationContextsProviderContext context, LayoutSegment segment, Set<String> resolvedSteps) {
        Set<LinkMetaBlock> foundRunIdBlocks = new HashSet<>();
        Set<String> runIds = findRunIds(linkMetaBlocks);
        
        log.info("Found runIds: {}", runIds);
        
        runIds.forEach(runId -> {
               Set<LinkMetaBlock> foundBlocks = queryByRunId(context.getSupplyChainId(), runId,
                       segment.getName(),
                       resolvedSteps);
               log.info("[{}] LinkMetaBlocks found for: supply chain id: [{}] segment: [{}] runId: [{}] and already resolved steps", foundBlocks.size(), context.getSupplyChainId(), segment.getName(), runId);
               foundRunIdBlocks.addAll(foundBlocks);
               
            }
        );
        return foundRunIdBlocks;
    }
    
    /*
     *     query(segment, step)
        return query in database links with segment and step and all match rules with rule.rule.type and step.rule.artifacts
     * 
     */    
    private Set<LinkMetaBlock> queryByArtifacts(String supplyChainId, String destinationSegmentName,
            String destStepName, Map<String, Map<MatchRule, Set<Artifact>>> destStepMap) {
        EnumMap<ArtifactType, Set<Artifact>> artifactTypeHashes = new EnumMap<>(ArtifactType.class);
        destStepMap.get(destStepName).keySet().forEach(rule -> {
            Set<Artifact> artifacts = destStepMap.get(destStepName).get(rule);
            artifactTypeHashes.putIfAbsent(rule.getDestinationType(), new HashSet<>());
            artifactTypeHashes.get(rule.getDestinationType()).addAll(artifacts);
        });
        return new HashSet<>(
                linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
                        supplyChainId, destinationSegmentName, destStepName, artifactTypeHashes));
    }

    private Set<String> findRunIds(Set<LinkMetaBlock> linkSets) {
        return linkSets
                .stream()
                .map(linkSet -> linkSet.getLink().getRunId())
                .collect(Collectors.toSet());
    }

    private Set<LinkMetaBlock> queryByRunId(String supplyChainId, String runId, String destinationSegmentName, Set<String> resolvedSteps) {
        return new HashSet<>(linkMetaBlockRepository.findByRunId(supplyChainId, destinationSegmentName, runId, resolvedSteps));
    }
}
