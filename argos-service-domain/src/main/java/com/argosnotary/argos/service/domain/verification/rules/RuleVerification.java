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
