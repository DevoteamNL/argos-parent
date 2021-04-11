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

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
    public List<VerificationContext> createPossibleVerificationContexts(LayoutMetaBlock layoutMetaBlock, Set<Artifact> productsToVerify) {
        Set<Set<LinkMetaBlock>> linkMetaBlockSets = new HashSet<>();
        // create context
        Set<LinkMetaBlock> links = new HashSet<>(linkMetaBlockRepository.findBySupplyChainId(layoutMetaBlock.getSupplyChainId()));
        if (!links.isEmpty()) {
            // get links for other steps
            links.addAll(linkMetaBlockRepository.findBySupplyChainId(
                    layoutMetaBlock.getSupplyChainId()));
            // permutate
            linkMetaBlockSets = permutateOnSteps(links);
        }

        log.info("processExpProductsMatchRules resulted in: {} possible verificationContexts", linkMetaBlockSets.size());
        
        return linkMetaBlockSets
                .stream()
                .map(linkSet -> VerificationContext
                        .builder()
                        .layoutMetaBlock(layoutMetaBlock)
                        .linkMetaBlocks(new ArrayList<>(linkSet))
                        .productsToVerify(productsToVerify).build())
                .collect(Collectors.toList());
    }
    
    static Set<Set<LinkMetaBlock>> permutateOnSteps(Set<LinkMetaBlock> linkMetaBlocks) {
        Set<Set<LinkMetaBlock>> tempSets = new HashSet<>();
        tempSets.add(new HashSet<>());
        Map<String, Map<Link, Set<LinkMetaBlock>>> stepSets = linkMetaBlocks.stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName(),
                        groupingBy(LinkMetaBlock::getLink, toSet())));
        for (Entry<String, Map<Link, Set<LinkMetaBlock>>> stepEntry : stepSets.entrySet()) {
            Set<Set<LinkMetaBlock>> newTempSets = new HashSet<>();
            for (Entry<Link, Set<LinkMetaBlock>> linkEntry : stepEntry.getValue().entrySet()) {
                for (Set<LinkMetaBlock> set : tempSets) {
                    Set<LinkMetaBlock> newSet = new HashSet<>(set);
                    newSet.addAll(linkEntry.getValue());
                    newTempSets.add(newSet);
                }
            }
            tempSets = newTempSets;
        }
        return tempSets;
    }
}
