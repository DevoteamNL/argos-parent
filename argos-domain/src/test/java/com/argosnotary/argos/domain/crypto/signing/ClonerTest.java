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
package com.argosnotary.argos.domain.crypto.signing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.argosnotary.argos.domain.layout.Layout;
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
        assertNull(Mappers.getMapper(Cloner.class).clone((List<Rule>)null));
        assertNull(Mappers.getMapper(Cloner.class).cloneSteps((List<Step>)null));
        assertNull(Mappers.getMapper(Cloner.class).cloneArtifacts((List<Artifact>)null));
    }

}
