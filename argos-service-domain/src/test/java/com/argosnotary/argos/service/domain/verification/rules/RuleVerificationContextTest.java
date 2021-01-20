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

import com.argosnotary.argos.domain.layout.rule.Rule;
import com.argosnotary.argos.domain.layout.rule.RuleType;
import com.argosnotary.argos.service.domain.verification.ArtifactsVerificationContext;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RuleVerificationContextTest {
    

    
    @Mock
    private ArtifactsVerificationContext artifactsContext;

    private Rule rule = new Rule(RuleType.ALLOW, "someDir/*.jar");
    
    @Test
    void nonNull() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            RuleVerificationContext.builder()
            .artifactsContext(artifactsContext)
            .rule(null)
            .build(); 
          });
        assertEquals("rule is marked non-null but is null", exception.getMessage());
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
            RuleVerificationContext.builder()
            .artifactsContext(null)
            .rule(rule)
            .build(); 
          });
        assertEquals("artifactsContext is marked non-null but is null", exception.getMessage());
    }
}
