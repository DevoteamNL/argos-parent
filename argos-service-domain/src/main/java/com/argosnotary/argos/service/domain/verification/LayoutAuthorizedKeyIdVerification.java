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

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.argosnotary.argos.domain.crypto.Signature;

import java.util.Optional;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.LAYOUT_AUTHORIZED_KEYID;

@Component
@Slf4j
public class LayoutAuthorizedKeyIdVerification implements Verification {

    @Override
    public Priority getPriority() {
        return LAYOUT_AUTHORIZED_KEYID;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        Optional<Signature> failedLayoutAuthorizedKeyIdVerification = context.getLayoutMetaBlock().getSignatures()
                .stream()
                .filter(signature -> layoutWasNotSignedByAuthorizedFunctionary(context, signature))
                .findFirst();
        failedLayoutAuthorizedKeyIdVerification
                .ifPresent(signature ->
                        log.info("failed verification authorizedkeys: {} , signature key: {}",
                                context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds(),
                                signature.getKeyId()
                        )
                );
        return VerificationRunResult.builder().runIsValid(failedLayoutAuthorizedKeyIdVerification.isEmpty()).build();
    }

    private static boolean layoutWasNotSignedByAuthorizedFunctionary(VerificationContext context, Signature signature) {
        return !context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds().contains(signature.getKeyId());
    }
}
