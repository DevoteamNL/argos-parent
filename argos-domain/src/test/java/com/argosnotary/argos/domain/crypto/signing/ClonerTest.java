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
package com.argosnotary.argos.domain.crypto.signing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;

class ClonerTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void nullsTest() {
        assertNull(Mappers.getMapper(Cloner.class).clone((Link)null));
        assertNull(Mappers.getMapper(Cloner.class).clone((Artifact)null));
        assertNull(Mappers.getMapper(Cloner.class).clone((Layout)null));
        assertNull(Mappers.getMapper(Cloner.class).clone((Step)null));
        assertNull(Mappers.getMapper(Cloner.class).clone((MatchRule)null));
        assertNull(Mappers.getMapper(Cloner.class).clone((LayoutSegment)null));
        assertNull(Mappers.getMapper(Cloner.class).clone((List<Rule>)null));
        assertNull(Mappers.getMapper(Cloner.class).cloneSteps((List<Step>)null));
        assertNull(Mappers.getMapper(Cloner.class).cloneArtifacts((List<Artifact>)null));
        assertNull(Mappers.getMapper(Cloner.class).cloneLayoutSegments((List<LayoutSegment>)null));
    }

}
