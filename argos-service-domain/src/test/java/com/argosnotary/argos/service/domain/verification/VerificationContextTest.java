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

import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationContextTest {
    public static final String STEP_NAME = "stepName";
    public static final Step STEP = Step.builder().name(STEP_NAME).build();
    private VerificationContext verificationContext;

    private List<LinkMetaBlock> linkMetaBlocks;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;

    @BeforeEach
    void setup() {

        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .stepName(STEP_NAME).build()).build()));
        verificationContext = VerificationContext
                .builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(linkMetaBlocks)
                .productsToVerify(Set.of())
                .build();
    }

    @Test
    void getLayoutMetaBlock() {
        assertThat(verificationContext.getLayoutMetaBlock(), sameInstance(layoutMetaBlock));
    }

    @Test
    void removeLinkMetaBlocks() {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(Collections.singletonList(STEP));
        assertThat(verificationContext.getLinkMetaBlocks(), contains(linkMetaBlocks.get(0)));
        assertThat(verificationContext.getStepNameLinkMetaBlockMap().get(STEP_NAME), contains(linkMetaBlocks.get(0)));
        
        verificationContext.removeLinkMetaBlocks(linkMetaBlocks);
        assertThat(verificationContext.getLinkMetaBlocks(), empty());
        assertThat(verificationContext.getStepNameLinkMetaBlockMap().get(STEP_NAME), empty());
    }
    
    @Test
    void getStepNameLinkMetaBlockMap() {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(Collections.singletonList(STEP));
        assertThat(verificationContext.getStepNameLinkMetaBlockMap().get(STEP_NAME), contains(linkMetaBlocks.get(0)));
        assertNull(verificationContext.getStepNameLinkMetaBlockMap().get("incorrect"));
    }
    
    @Test
    void getLinksByStep() {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getSteps()).thenReturn(Collections.singletonList(STEP));
        Map<String, Link> linkMap = verificationContext.getStepLinkMap();
        assertThat(linkMap.keySet(), contains(STEP_NAME));
        assertThat(linkMap.get(STEP_NAME), is(linkMetaBlocks.get(0).getLink()));
    }
    
    @Test
    void nonNull() {
        Throwable exception = assertThrows(java.lang.NullPointerException.class, () -> {
            VerificationContext.builder()
                .layoutMetaBlock(null)
                .build(); 
          });
        assertEquals("layoutMetaBlock is marked non-null but is null", exception.getMessage());
        
        exception = assertThrows(java.lang.NullPointerException.class, () -> {
            VerificationContext.builder()
                .linkMetaBlocks(null)
                .build(); 
          });
        assertEquals("linkMetaBlocks is marked non-null but is null", exception.getMessage());
    }
    
}
