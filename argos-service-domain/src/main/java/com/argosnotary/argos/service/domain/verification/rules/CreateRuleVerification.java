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

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CreateRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.CREATE;
    }

    @Override
    public boolean verify(RuleVerificationContext<? extends Rule> context) {
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts();
        
        Set<String> uris = context.getProducts().stream().map(Artifact::getUri).collect(Collectors.toSet());
        
        uris.removeAll(context.getMaterials().stream().map(Artifact::getUri).collect(Collectors.toSet()));
        
        if (filteredArtifacts.stream().map(Artifact::getUri).allMatch(uris::contains)) {
            context.consume(filteredArtifacts);
            logInfo(log, filteredArtifacts);
            return true;
        } else {
            logErrors(log, filteredArtifacts);
            return false;
        }
    }
}
