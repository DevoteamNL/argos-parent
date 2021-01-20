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


import com.argosnotary.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

import static java.util.Collections.emptySet;

@Builder
@Getter
@ToString
public class RuleVerificationResult {

    private boolean valid;

    private final Set<Artifact> validatedArtifacts;

    public static RuleVerificationResult okay(Set<Artifact> validatedArtifacts) {
        return RuleVerificationResult.builder().valid(true).validatedArtifacts(validatedArtifacts).build();
    }

    public static RuleVerificationResult notOkay() {
        return RuleVerificationResult.builder().valid(false).validatedArtifacts(emptySet()).build();
    }
}
