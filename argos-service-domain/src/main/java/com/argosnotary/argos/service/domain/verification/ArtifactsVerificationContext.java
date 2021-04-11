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

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@Getter
public class ArtifactsVerificationContext {

    private final Map<String, Link> linksMap;
    
    private final Link link;
    
    @NonNull
    private Set<Artifact> notConsumedArtifacts;
    
    @Builder.Default
    private Set<Artifact> consumedArtifacts = new HashSet<>();

    public Set<Artifact> getFilteredArtifacts(String pattern) {
        return getFilteredArtifacts(pattern, null);
    }

    public Set<Artifact> getFilteredArtifacts(String pattern, String prefix) {
        return filterArtifacts(notConsumedArtifacts, pattern, prefix);
    }

    public static Set<Artifact> filterArtifacts(Set<Artifact> artifacts, String pattern, @Nullable String prefix) {
        return artifacts.stream()
        		.filter(artifact -> hasPrefix(artifact, prefix))
        		.filter(artifact -> ArtifactMatcher.matches(getUri(artifact, prefix), pattern)).collect(Collectors.toSet());
    }
    
    private static boolean hasPrefix(Artifact artifact, @Nullable String prefix) {
    	return (StringUtils.hasLength(prefix) && artifact.getUri().startsWith(prefix)) || !StringUtils.hasLength(prefix);
    }

    private static String getUri(Artifact artifact, String prefix) {
        if (StringUtils.hasLength(prefix) && artifact.getUri().startsWith(prefix)) {
            return Paths.get(prefix).relativize(Paths.get(artifact.getUri())).toString();
        } else {
            return artifact.getUri();
        }
    }
    
    public Optional<Link> getLinkByStepName(String stepName) {
        if (linksMap == null || linksMap.get(stepName) == null) {
            return Optional.empty();
        }
        return Optional.of(linksMap.get(stepName));
    }

    public Set<Artifact> getMaterials() {
        return new HashSet<>(link.getMaterials());
    }
    
    public Set<Artifact> getProducts() {
        return new HashSet<>(link.getProducts());
    }
    
    public void consume(Set<Artifact> artifacts) {
        notConsumedArtifacts.removeAll(artifacts);
        consumedArtifacts.addAll(artifacts);
    }
}
