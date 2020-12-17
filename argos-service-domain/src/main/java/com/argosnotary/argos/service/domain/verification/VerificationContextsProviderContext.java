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

import static java.util.stream.Collectors.groupingBy;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerification;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerificationContext;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Builder
@Getter
@Setter
@Slf4j
public class VerificationContextsProviderContext {
    private Map<RuleType, RuleVerification> rulesVerificationMap;
    
    private String supplyChainId;
    
    private Layout layout;
    
    private Set<Artifact> productsToVerify;

    private Set<LayoutSegment> resolvedSegments;
    private Set<Set<LinkMetaBlock>> linkMetaBlockSets;
    
    private Queue<LayoutSegment> topologicalSortedSegments;
    
    private Map<LayoutSegment, Set<LayoutSegment>> segmentGraph;
    
    
    /*
     * 
     */
    public void init() {
        linkMetaBlockSets = new HashSet<>();
        resolvedSegments = new HashSet<>();
        if (layout != null) {
            segmentGraph = createDirectedSegmentGraph(layout);
            topologicalSortedSegments = topologicalSort(createDirectedSegmentGraph(layout));
            
        }
    }
    
    /*
     * gets next LayoutSegment to process
     * 
     * @return first element in the queue
     */
    public Optional<LayoutSegment> getNextSegment() {
        if (topologicalSortedSegments.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(topologicalSortedSegments.remove());
        }
    }
    
    /*
     * Topological sort a directed graph by Kahn's algorithm
     * 
     * @param startSegment
     * @return
     */
    public static Queue<LayoutSegment> topologicalSort(Map<LayoutSegment, Set<LayoutSegment>> graph) {
        Set<LayoutSegment> nodesWithoutIncomingEdges = graph.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Entry<LayoutSegment, Set<LayoutSegment>>::getKey)
                .collect(toSet());
        LinkedList<LayoutSegment> sortedList = new LinkedList<>();
        while (!nodesWithoutIncomingEdges.isEmpty()) {
            LayoutSegment n = nodesWithoutIncomingEdges.iterator().next();
            sortedList.add(n);
            graph.keySet().forEach(node -> {
                if (graph.get(node).contains(n)) {
                    graph.get(node).remove(n);
                    if (graph.get(node).isEmpty()) {                        
                        nodesWithoutIncomingEdges.add(node);
                    }
                }
            });
            nodesWithoutIncomingEdges.remove(n);
        }
        graph.entrySet().forEach(entry -> {if (!entry.getValue().isEmpty()) {
                log.error("Layout segment graph has at least 1 cycle");
                for (LayoutSegment segment: entry.getValue()) {
                    log.error("Edge not processed: [{} <= {}]", entry.getKey().getName(), segment.getName());
                }
                throw new ArgosError("layout segment graph has at least 1 cycle.");
            }
        });
        return sortedList;
    }
    
    /*
     * 
     * @return
     */
    public static Map<LayoutSegment, Set<LayoutSegment>> createDirectedSegmentGraph(Layout layout) {
        Map<LayoutSegment, Set<LayoutSegment>> graph = new HashMap<>();

        layout.getLayoutSegments().forEach(segment -> 
            segment.getSteps().forEach(step -> {
                Set<String> destSegs = Stream
                        .concat(step.getExpectedMaterials().stream(),
                                step.getExpectedProducts().stream())
                        .filter(rule -> RuleType.MATCH.equals(rule.getRuleType()))
                        .filter(rule -> !segment.getName().equals(((MatchRule) rule).getDestinationSegmentName()))
                        .map(rule -> ((MatchRule) rule).getDestinationSegmentName()).collect(toSet());
                layout.getLayoutSegments().forEach(destSegment -> {
                    graph.putIfAbsent(destSegment, new HashSet<>());
                    if (destSegs.contains(destSegment.getName())) {
                        graph.get(destSegment).add(segment);
                    }
                });
            }));
        if (graph.values().stream().filter(Set::isEmpty).count() > 1) {
            throw new ArgosError("layout segment graph has more than 1 start segment.");
        }
        return graph;
    }
    
    /*
     * Ge
     * @return
     */
    public Map<String, Map<MatchRule, Set<Artifact>>> getFirstMatchRulesAndArtifacts() {
        Map<String, Map<MatchRule, Set<Artifact>>> destStepMap = new HashMap<>();
        layout.getExpectedEndProducts().forEach(rule -> {
            destStepMap.putIfAbsent(rule.getDestinationStepName(), new HashMap<>());
            destStepMap.get(rule.getDestinationStepName()).putIfAbsent(rule, new HashSet<>());
            destStepMap.get(rule.getDestinationStepName())
                .get(rule)
                .addAll(getDestinationArtifacts(productsToVerify, rule));            
        });
        return destStepMap;
    }
    
    public static Set<Artifact> getDestinationArtifacts(Set<Artifact> notConsumed, MatchRule rule) {
        Set<Artifact> destArtifacts = ArtifactsVerificationContext.filterArtifacts(notConsumed, 
                rule.getPattern(),
                rule.getSourcePathPrefix());
        notConsumed.removeAll(destArtifacts);
        if (rule.getSourcePathPrefix() != null || rule.getDestinationPathPrefix() != null) {
            destArtifacts = destArtifacts.stream().map(artifact -> {
                Artifact newArtifact = new Artifact(artifact.getUri(), artifact.getHash());
                if (rule.getSourcePathPrefix() != null && newArtifact.getUri().startsWith(rule.getSourcePathPrefix())) {
                    newArtifact.setUri(newArtifact.getUri().substring(rule.getSourcePathPrefix().length()));
                }
                if (rule.getDestinationPathPrefix() != null) {
                    newArtifact.setUri(Paths.get(rule.getDestinationPathPrefix(), newArtifact.getUri()).toString());
                }
                return newArtifact;
            }).collect(toSet());
        } 
        return destArtifacts;        
    }
    
    public Map<String, Map<MatchRule, Set<Artifact>>> getMatchRulesAndArtifacts(LayoutSegment destinationSegment, Set<LinkMetaBlock> linkMetaBlockSet) {
        Map<String, Map<String, Set<Link>>> blockMap = linkMetaBlockSet.stream().map(LinkMetaBlock::getLink)
                .collect(groupingBy(Link::getLayoutSegmentName,
                        groupingBy(Link::getStepName, toSet())));
        
        Map<String, Map<Step, Set<Link>>> linkMap = new HashMap<>();
        segmentGraph.get(destinationSegment).forEach(segment -> {
            linkMap.putIfAbsent(segment.getName(), new HashMap<>());
            segment.getSteps().forEach(step -> {
                linkMap.get(segment.getName()).putIfAbsent(step, new HashSet<>());
                if (blockMap.containsKey(segment.getName()) && blockMap.get(segment.getName()).containsKey(step.getName())) {
                    linkMap.get(segment.getName()).get(step).addAll(blockMap.get(segment.getName()).get(step.getName()));        
                }
        });});        
        
        Map<String, Map<MatchRule, Set<Artifact>>> destStepMap = new HashMap<>();
        linkMap.entrySet().forEach(segmentEntry -> 
            segmentEntry.getValue().entrySet().forEach(stepEntry -> 
                stepEntry.getValue().forEach(link -> {
                    addStepsWithMatchRulesAndArtifactsToMap(destStepMap, link, destinationSegment, stepEntry.getKey().getExpectedMaterials(), ArtifactType.MATERIALS);
                    addStepsWithMatchRulesAndArtifactsToMap(destStepMap, link, destinationSegment, stepEntry.getKey().getExpectedProducts(), ArtifactType.PRODUCTS);
                })));
        return destStepMap;
    }
    
    public void addStepsWithMatchRulesAndArtifactsToMap(
            Map<String, Map<MatchRule, Set<Artifact>>> destStepMap,
            Link link, 
            LayoutSegment destSegment,
            List<? extends Rule> rules,
            ArtifactType type) {
        List<Artifact> srcArtifacts = ArtifactType.MATERIALS.equals(type) ? link.getMaterials() : link.getProducts();
        ArtifactsVerificationContext artifactsContext = ArtifactsVerificationContext.builder()
                .segmentName(link.getLayoutSegmentName())
                .link(link)
                .notConsumedArtifacts(new HashSet<>(srcArtifacts)).build();
        rules.forEach(rule -> {
            // match rule which points to destination segment
            if (RuleType.MATCH.equals(rule.getRuleType()) && destSegment.getName().equals(((MatchRule) rule).getDestinationSegmentName())) {
                MatchRule matchRule = (MatchRule) rule;
                destStepMap.putIfAbsent(matchRule.getDestinationStepName(), new HashMap<>());
                destStepMap.get(matchRule.getDestinationStepName())
                    .put(matchRule, 
                            getDestinationArtifacts(artifactsContext.getNotConsumedArtifacts(), matchRule));
                    // consume artifacts
                getRuleVerification(matchRule.getRuleType())
                        .verify(RuleVerificationContext.builder()
                                .rule(rule)
                                .artifactsContext(artifactsContext).build());
            } else { // all other rules
                // process first other rule types
                // consume filtered artifacts
                getRuleVerification(rule.getRuleType())
                    .verify(RuleVerificationContext.builder().rule(rule).artifactsContext(artifactsContext).build());
            }
        });
    }

    private RuleVerification getRuleVerification(RuleType type) {
        return rulesVerificationMap.get(type);
    }
    
    public static Set<Set<LinkMetaBlock>> permutateAndAddLinkMetaBlocks(Set<LinkMetaBlock> linkMetaBlocks, Set<Set<LinkMetaBlock>> linkMetaBlockSets) {
        Set<Set<LinkMetaBlock>> segmentLinkSets = permutateOnStepsInSegment(linkMetaBlocks);
        Set<Set<LinkMetaBlock>> tempSets = new HashSet<>();
        for (Set<LinkMetaBlock> linkSet: linkMetaBlockSets) {
            for (Set<LinkMetaBlock> segmentSet: segmentLinkSets) {
                Set<LinkMetaBlock> newSet = new HashSet<>(linkSet);
                newSet.addAll(segmentSet);
                tempSets.add(newSet);
            }
        }
        return tempSets;
    }
    
    public static Set<Set<LinkMetaBlock>> permutateOnStepsInSegment(Set<LinkMetaBlock> linkMetaBlocks) {
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