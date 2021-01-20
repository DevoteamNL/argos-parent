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

import java.util.Optional;
import java.util.Set;

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.domain.verification.ArtifactsVerificationContext;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Builder
@Getter
@ToString
public class RuleVerificationContext<R extends Rule> {
    
    @NonNull
    private ArtifactsVerificationContext artifactsContext;
    
    @NonNull
    private final R rule;
    
    public Set<Artifact> getFilteredArtifacts() {
        return artifactsContext.getFilteredArtifacts(rule.getPattern(), null);
    }

    public Set<Artifact> getFilteredArtifacts(String prefix) {
        return artifactsContext.getFilteredArtifacts(rule.getPattern(), prefix);
    }
    
    public void consume(Set<Artifact> artifacts) {
        artifactsContext.consume(artifacts);
    }
    
    public String getSegmentName() {
        return artifactsContext.getSegmentName();
    }
    
    public Optional<Link> getLinkBySegmentNameAndStepName(String segmentName, String stepName) {
        return artifactsContext.getLinkBySegmentNameAndStepName(segmentName, stepName);
    }
    
    public Set<Artifact> getMaterials() {
        return artifactsContext.getMaterials();
    }
    
    public Set<Artifact> getProducts() {
        return artifactsContext.getProducts();
    }

    public <T extends Rule> T getRule() {
        return (T) rule;
    }
}
