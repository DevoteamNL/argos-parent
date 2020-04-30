/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.ArtifactsVerificationContext;

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
    
    private String segmentName = "segmentName";
    
    private String segmentName2 = "segmentName2";
    
    private Step step;
    
    private Step step2;
    
    private Link link;
    
    private ArtifactType type = ArtifactType.MATERIALS;
    
    private Map<String, Map<String, Link>> linksMap;

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
        Map<String, Link> stepmap = new HashMap<>();
        stepmap.put(step.getName(), link);
        linksMap.put(segmentName, stepmap);
        Map<String, Link> stepmap2 = new HashMap<>();
        stepmap.put(step2.getName(), null);
        linksMap.put(segmentName2, stepmap2);
        
        verificationContext1 = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
                .notConsumedArtifacts(Set.of(artifact1, artifact2))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext2 = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
                .notConsumedArtifacts(Set.of(artifact1, artifact2))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext3 = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
                .notConsumedArtifacts(Set.of(artifact1, artifact2))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext4 = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
                .notConsumedArtifacts(Set.of(artifact1, artifact2, artifact3, artifact4))
                .link(link)
                .linksMap(linksMap)
                //.rule(ruleWithPrefix)
                .build();
        verificationContext5 = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
                .notConsumedArtifacts(Set.of(artifact1, artifact2, artifact3, artifact4))
                .link(link)
                .linksMap(linksMap)
                .build();
        verificationContext6 = ArtifactsVerificationContext.builder()
                .segmentName(segmentName)
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
        assertEquals(Optional.of(link), verificationContext1.getLinkBySegmentNameAndStepName(segmentName, step.getName()));
        assertThat(verificationContext1.getLinkBySegmentNameAndStepName(segmentName, "foo"), is(Optional.empty()));
        assertThat(verificationContext1.getLinkBySegmentNameAndStepName("foo", step.getName()), is(Optional.empty()));
    }
    
    @Test
    void nonNull() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            ArtifactsVerificationContext.builder()
            .segmentName(null)
            .notConsumedArtifacts(Set.of(artifact1, artifact2))
            .linksMap(linksMap)
            .build();
          });
        assertEquals("segmentName is marked non-null but is null", exception.getMessage());

        exception = assertThrows(java.lang.NullPointerException.class, () -> {
            ArtifactsVerificationContext.builder()
            .segmentName(segmentName)
            .notConsumedArtifacts(null)
            .linksMap(linksMap)
            .build();
          });
        assertEquals("notConsumedArtifacts is marked non-null but is null", exception.getMessage());
    }
}
