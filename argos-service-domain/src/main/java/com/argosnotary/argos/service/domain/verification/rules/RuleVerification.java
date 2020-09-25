/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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

import java.util.Set;

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.domain.link.Artifact;

import org.slf4j.Logger;

public interface RuleVerification {
    
    RuleType getRuleType();

    boolean verify(RuleVerificationContext<? extends Rule> context);
    
    public default void logInfo(Logger log, Set<Artifact> artifacts) {
        log.info("verify result for [{}] rule was valid, number of consumed artifacts [{}]",
                getRuleType(),
                artifacts.size());
    }
    
    public default void logErrors(Logger log, Set<Artifact> artifacts) {
        artifacts.stream().forEach(artifact -> log.info("On rule type [{}] not consumed artifact: [{}]", 
                    getRuleType(),
                    artifact)
        );
    }

}
