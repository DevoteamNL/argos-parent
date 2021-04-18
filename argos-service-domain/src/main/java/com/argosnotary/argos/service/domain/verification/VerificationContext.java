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

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ToString
public class VerificationContext {
    @Getter
    private final List<LinkMetaBlock> linkMetaBlocks;
    @Getter
    private final List<LinkMetaBlock> originalLinkMetaBlocks;
    @Getter
    private final LayoutMetaBlock layoutMetaBlock;
    
    @Getter
    private final Set<Artifact> artifactsToRelease;

    @Builder
    public VerificationContext(@NonNull List<LinkMetaBlock> linkMetaBlocks, 
            @NonNull LayoutMetaBlock layoutMetaBlock, Set<Artifact> productsToVerify) {
        this.linkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.originalLinkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.layoutMetaBlock = layoutMetaBlock;
        this.artifactsToRelease = new HashSet<>(productsToVerify);
    }
    
    public void removeLinkMetaBlocks(List<LinkMetaBlock> linkMetaBlocksToRemove) {
        linkMetaBlocks.removeAll(linkMetaBlocksToRemove);
    }
    
    public Map<String, Set<LinkMetaBlock>> getStepNameLinkMetaBlockMap() {
        Map<String, Set<LinkMetaBlock>> stepNameLinkMetaBlockMap = new HashMap<>();
        layoutMetaBlock.getLayout().getSteps()
        .forEach(step -> stepNameLinkMetaBlockMap
                .putIfAbsent(step.getName(), new HashSet<>()));
        linkMetaBlocks.forEach(l -> {
            if (stepNameLinkMetaBlockMap.get(l.getLink().getStepName()) != null) {
                stepNameLinkMetaBlockMap.get(l.getLink().getStepName()).add(l);
            }
        });
        return stepNameLinkMetaBlockMap;
    }
    
    public Map<String, Link> getStepLinkMap() {
        Map<String, Set<LinkMetaBlock>> stepLinkMetaBlockMap = getStepNameLinkMetaBlockMap();
        Map<String, Link> stepLinkMap = new HashMap<>();        
        stepLinkMetaBlockMap.entrySet().forEach(entry -> {
            Link link = entry.getValue().isEmpty() ? null : stepLinkMetaBlockMap.get(entry.getKey()).iterator().next().getLink();
            stepLinkMap.put(entry.getKey(), link);
        });
        return stepLinkMap;
    }
    
}
