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

import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.domain.verification.ArtifactsVerificationContext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

import static com.argosnotary.argos.domain.layout.ArtifactType.PRODUCTS;

import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
public class MatchRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.MATCH;
    }

    @Override
    public boolean verify(RuleVerificationContext<? extends Rule> context) {
        MatchRule rule = context.getRule();
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts(rule.getSourcePathPrefix());
        
        String destinationSegmentName = rule.getDestinationSegmentName() != null ? rule.getDestinationSegmentName() : context.getSegmentName();

        Optional<Link> optionalLink = context.getLinkBySegmentNameAndStepName(destinationSegmentName, rule.getDestinationStepName());
        
        if (optionalLink.isPresent()) {
            Link link = optionalLink.get();
            Set<Artifact> filteredDestinationArtifacts = null;
            if (rule.getDestinationType() == PRODUCTS) {
                filteredDestinationArtifacts = new HashSet<>(link.getProducts());                
            } else {
                filteredDestinationArtifacts = new HashSet<>(link.getMaterials());
            }
            filteredDestinationArtifacts = ArtifactsVerificationContext.filterArtifacts(filteredDestinationArtifacts, rule.getPattern(), rule.getDestinationPathPrefix());
            if (verifyArtifacts(filteredArtifacts, filteredDestinationArtifacts)) {
                context.consume(filteredArtifacts);
                logInfo(log, filteredArtifacts);
                return true;
            } else {
                logErrors(log, filteredArtifacts);
                return false;
            }
        } else {
            log.warn("no link for destination step {}", rule.getDestinationStepName());
            return false;
        }
    }

    private boolean verifyArtifacts(Set<Artifact> filteredSourceArtifacts, Set<Artifact> filteredDestinationArtifacts) {
        return filteredSourceArtifacts
                .stream()
                .map(Artifact::getHash)
                .allMatch(filteredDestinationArtifacts
                        .stream()
                        .map(Artifact::getHash)
                        .collect(Collectors.toSet())::contains);
    }    

}
