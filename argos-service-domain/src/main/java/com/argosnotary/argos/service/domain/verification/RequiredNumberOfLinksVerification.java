/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
