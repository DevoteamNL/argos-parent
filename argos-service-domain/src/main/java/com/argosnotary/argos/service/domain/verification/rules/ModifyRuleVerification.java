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
