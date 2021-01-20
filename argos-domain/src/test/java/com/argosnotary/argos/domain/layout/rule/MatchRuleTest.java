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
package com.argosnotary.argos.domain.layout.rule;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.layout.ArtifactType;

class MatchRuleTest {

    @Test
    void nonNullTest() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            MatchRule rule = new MatchRule(null, null, ArtifactType.MATERIALS, null, null, null); 
          });
        
        assertEquals("pattern is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
            MatchRule rule = new MatchRule("", null, null, null, null, null); 
          });
        
        assertEquals("destinationType is marked non-null but is null", exception.getMessage());
        
    }
    
    @Test
    void toStringTest() {
        MatchRule rule = MatchRule.builder()
                .destinationPathPrefix("destinationPathPrefix")
                .destinationSegmentName("destinationSegmentName")
                .destinationType(ArtifactType.MATERIALS)
                .pattern("**")
                .build();
        
        assertEquals("MatchRule(sourcePathPrefix=null, destinationType=MATERIALS, destinationPathPrefix=destinationPathPrefix, destinationSegmentName=destinationSegmentName, destinationStepName=null)", rule.toString());
        
    }

}
