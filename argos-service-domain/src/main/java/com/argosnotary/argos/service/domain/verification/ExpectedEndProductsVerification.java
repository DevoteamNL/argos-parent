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
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.domain.verification.rules.RuleVerification;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.EXPECTED_END_PRODUCTS;;

@Component
@RequiredArgsConstructor
@Slf4j
@ToString
public class ExpectedEndProductsVerification implements Verification {

    private final List<RuleVerification> ruleVerificationList;

    private Map<RuleType, RuleVerification> rulesVerificationMap = new EnumMap<>(RuleType.class);

    @Override
    public Priority getPriority() {
        return EXPECTED_END_PRODUCTS;
    }

    @PostConstruct
    public void init() {
        ruleVerificationList.forEach(ruleVerification -> rulesVerificationMap.put(ruleVerification.getRuleType(), ruleVerification));
    }

    @Override
    public VerificationRunResult verify(VerificationContext verificationContext) {
        Map<String, Link> linksMap = verificationContext.getStepLinkMap();
                
        Set<Artifact> matchedArtifacts = getMatchedArtifacts(linksMap, verificationContext.getLayoutMetaBlock().getLayout().getExpectedEndProducts());
        Set<Artifact> artifactsToRelease = verificationContext.getArtifactsToRelease();
        
        return VerificationRunResult.builder().runIsValid(matchedArtifacts.containsAll(artifactsToRelease) && artifactsToRelease.containsAll(matchedArtifacts)).build();
    }

    private Set<Artifact> getMatchedArtifacts(Map<String, Link> linksMap, List<MatchRule> expectedEndProducts) {
        Set<Artifact> matchedArtifacts = new HashSet<>();
        for (MatchRule rule : expectedEndProducts) {
            Link link = linksMap.get(rule.getDestinationStepName());
            Set<Artifact> tempArtifacts = new HashSet<>();
            if (rule.getDestinationType().equals(ArtifactType.MATERIALS)) {
                tempArtifacts.addAll(link.getMaterials());
            } else {
                tempArtifacts.addAll(link.getProducts());
            }
            matchedArtifacts.addAll(ArtifactsVerificationContext
                    .filterArtifacts(tempArtifacts, rule.getPattern(), rule.getDestinationPathPrefix())
                    .stream()
                    .map(artifact -> normalize(artifact, rule.getSourcePathPrefix(), rule.getDestinationPathPrefix())).collect(Collectors.toSet()));            
            
        }
        return matchedArtifacts;
    }
    
    private Artifact normalize(Artifact artifact, String srcPrefix, String destPrefix) {
        String uri = ArtifactsVerificationContext.getUri(artifact, destPrefix);
        if (StringUtils.hasLength(srcPrefix)) {
            uri = Paths.get(srcPrefix, uri).toString();
        }            
        return Artifact.builder()
                .hash(artifact.getHash())
                .uri(uri).build();
    }

}
