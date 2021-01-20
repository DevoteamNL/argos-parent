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
