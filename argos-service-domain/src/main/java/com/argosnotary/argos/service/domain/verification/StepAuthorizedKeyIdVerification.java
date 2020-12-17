/*
 * Copyright (C) 2020 Argos Notary
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

import com.argosnotary.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.STEP_AUTHORIZED_KEYID;

@Component
@Slf4j
public class StepAuthorizedKeyIdVerification implements Verification {

    @Override
    public Priority getPriority() {
        return STEP_AUTHORIZED_KEYID;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        List<LinkMetaBlock> failedLinkAuthorizedKeyIdVerifications = context
                .getLinkMetaBlocks()
                .stream()
                .filter(linkMetaBlock -> linkIsNotSignedByAuthorizedFunctionary(context, linkMetaBlock))
                .collect(Collectors.toList());

        if (!failedLinkAuthorizedKeyIdVerifications.isEmpty()) {
            failedLinkAuthorizedKeyIdVerifications
                .forEach(block -> log.info("LinkMetaBlock for step [{}] is signed with the not authorized key [{}] the linkMetaBlock will be removed from the context",
                        block.getLink().getStepName(), block.getSignature().getKeyId()));
            context.removeLinkMetaBlocks(failedLinkAuthorizedKeyIdVerifications);
        }

        return VerificationRunResult.okay();
    }

    private static boolean linkIsNotSignedByAuthorizedFunctionary(VerificationContext context, LinkMetaBlock linkMetaBlock) {
        return !context
                .getStepBySegmentNameAndStepName(
                        linkMetaBlock.getLink().getLayoutSegmentName(),
                        linkMetaBlock.getLink().getStepName()
                )
                .getAuthorizedKeyIds()
                .contains(linkMetaBlock.getSignature().getKeyId());
    }

}
