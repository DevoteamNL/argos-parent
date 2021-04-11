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
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.domain.verification.ArtifactsVerificationContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


@ExtendWith(MockitoExtension.class)
class ArtifactsVerificationContextTest {
    

    private ArtifactsVerificationContext verificationContext1;
    private ArtifactsVerificationContext verificationContext2;
    private ArtifactsVerificationContext verificationContext3;
    private ArtifactsVerificationContext verificationContext4;
    private ArtifactsVerificationContext verificationContext5;
    private ArtifactsVerificationContext verificationContext6;
    
    private Step step;
    
    private Step step2;
    
    private Link link;
    
    private ArtifactType type = ArtifactType.MATERIALS;
    
    private Map<String, Link> linksMap;

    private String patternWithPrefix = "someDir/*.jar";    
    private String patternWithSuffix = "*.jar";
    private String patternAllMatch = "**";
    private String patternNotFound = "*.foo";
    

    private String prefix = "someDir";

    private Artifact artifact1 = new Artifact("someDir/some.jar", "hash");
    private Artifact artifact2 = new Artifact("someDir/some.html", "hash");    
    private Artifact artifact3 = new Artifact("someDir/someOther.jar", "hash");
    private Artifact artifact4 = new Artifact("someDir/someOther.html", "hash");
    private Artifact artifact5 = new Artifact("root.html", "hash");

    @BeforeEach
    void setUp() {
        step = Step.builder().name("step").build();
        step2 = Step.builder().name("step2").build();
        link = Link.builder()
                .stepName(step.getName())
                .materials(List.of(artifact1, artifact2))
                .products(List.of(artifact1, artifact2, artifact3, artifact4)).build();
        linksMap = new HashMap<>();
        linksMap.put(step.getName(), link);
        linksMap.put(step2.getName(), null);
        
        verificationContext1 = ArtifactsVerificationContext.builder()
                .notConsumedArtifacts(Set.of(artifact1, artifact2))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext2 = ArtifactsVerificationContext.builder()
                .notConsumedArtifacts(Set.of(artifact1, artifact2))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext3 = ArtifactsVerificationContext.builder()
                .notConsumedArtifacts(Set.of(artifact1, artifact2))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext4 = ArtifactsVerificationContext.builder()
                .notConsumedArtifacts(Set.of(artifact1, artifact2, artifact3, artifact4))
                .link(link)
                .linksMap(linksMap)
                //.rule(ruleWithPrefix)
                .build();
        verificationContext5 = ArtifactsVerificationContext.builder()
                .notConsumedArtifacts(Set.of(artifact1, artifact2, artifact3, artifact4))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext6 = ArtifactsVerificationContext.builder()
                .notConsumedArtifacts(Set.of(artifact1, artifact2, artifact3, artifact4, artifact5))
                .link(link)
                .linksMap(linksMap)
                .build();
    }

    @Test
    void getFilteredArtifacts() {
        Set<Artifact> artifacts = verificationContext1.getFilteredArtifacts(patternWithPrefix);
        assertThat(artifacts, is(Set.of(artifact1)));
        
        artifacts = verificationContext3.getFilteredArtifacts(patternNotFound);
        assertThat(artifacts, empty());
        
        artifacts = verificationContext2.getFilteredArtifacts(patternWithPrefix);
        assertThat(artifacts, is(Set.of(artifact1)));
        
        artifacts = verificationContext4.getFilteredArtifacts(patternWithSuffix, "someDir/");
        assertThat(artifacts, is(Set.of(artifact1, artifact3)));
        
        artifacts = verificationContext5.getFilteredArtifacts(patternAllMatch);
        assertThat(artifacts, is(Set.of(artifact1, artifact2, artifact3, artifact4)));
        
        artifacts = verificationContext6.getFilteredArtifacts(patternAllMatch, prefix);
        assertThat(artifacts, is(Set.of(artifact1, artifact2, artifact3, artifact4)));
    }
    
    @Test
    void getArtifacts() {
        Set<Artifact> artifacts = verificationContext2.getFilteredArtifacts(patternWithSuffix, "someDir/");
        assertThat(artifacts, contains(artifact1));
    }
    
    @Test
    void getLinkBySegmentNameAndStepName() {
        assertEquals(Optional.of(link), verificationContext1.getLinkByStepName(step.getName()));
        assertThat(verificationContext1.getLinkByStepName("foo"), is(Optional.empty()));
        assertThat(verificationContext1.getLinkByStepName(step.getName()), is(Optional.of(link)));
    }
    
    @Test
    void nonNull() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            ArtifactsVerificationContext.builder()
            .notConsumedArtifacts(null)
            .linksMap(linksMap)
            .build();
          });
        assertEquals("notConsumedArtifacts is marked non-null but is null", exception.getMessage());
    }
}
