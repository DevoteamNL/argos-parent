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

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Component
@Slf4j
public class ModifyRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.MODIFY;
    }

    @Override
    public boolean verify(RuleVerificationContext<? extends Rule> context) {
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts();
        
        Set<String> uris = context.getFilteredArtifacts().stream().map(Artifact::getUri).collect(Collectors.toSet());

        Map<String, Set<Artifact>> uriMap = Stream.concat(
                context.getMaterials().stream().filter(artifact -> uris.contains(artifact.getUri())), 
                context.getProducts().stream().filter(artifact -> uris.contains(artifact.getUri())))
                .collect(groupingBy(Artifact::getUri, Collectors.toSet()));

        return uriMap.values().stream()
                .filter(artifacts -> artifacts.size() != 2)
                .map(artifacts -> {
                    logErrors(log, filteredArtifacts);
                    return false;
                })
                .findFirst()
                .orElseGet(() -> {
                    context.consume(filteredArtifacts);
                    logInfo(log, filteredArtifacts);
                    return true;
                });
    }
}
