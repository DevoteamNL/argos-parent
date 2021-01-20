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

import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.REQUIRED_NUMBER_OF_LINKS;
import static java.util.stream.Collectors.groupingBy;

@Component
@Slf4j
public class RequiredNumberOfLinksVerification implements Verification {
    @Override
    public Priority getPriority() {
        return REQUIRED_NUMBER_OF_LINKS;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        return context.getLayoutMetaBlock().getLayout().getLayoutSegments()
                .stream()
                .filter(segment -> !verifyForSegment(segment, context))
                .findFirst()
                .map(segment -> VerificationRunResult.builder().runIsValid(false).build())
                .orElse(VerificationRunResult.okay());

    }

    private boolean verifyForSegment(LayoutSegment segment, VerificationContext context) {
        Optional<String> invalidStep = context
                .getStepNamesBySegmentName(segment.getName())
                .stream()
                .filter(stepName -> !isValid(segment.getName(), stepName, context))
                .findFirst();

        return invalidStep.isEmpty();
    }

    private Boolean isValid(String segmentName, String stepName, VerificationContext context) {
        Map<Integer, Set<LinkMetaBlock>> linkMetaBlockMap = context
                .getLinkMetaBlocksBySegmentNameAndStepName(segmentName, stepName).stream()
                .collect(groupingBy(f -> f.getLink().hashCode(), Collectors.toSet()));
        if (linkMetaBlockMap.size() == 1) {
            return isValid(linkMetaBlockMap.values().iterator().next(), context.getStepBySegmentNameAndStepName(segmentName, stepName));
        } else {
            log.info("[{}] different link objects in metablocks for step [{}]", linkMetaBlockMap.size(), stepName);
            return false;
        }
    }

    private boolean isValid(Set<LinkMetaBlock> linkMetaBlocks, Step step) {
        log.info("[{}] links for step [{}] and should be at least [{}]", linkMetaBlocks.size(), step.getName(), step.getRequiredNumberOfLinks());
        return linkMetaBlocks.size() >= step.getRequiredNumberOfLinks();
    }
}
