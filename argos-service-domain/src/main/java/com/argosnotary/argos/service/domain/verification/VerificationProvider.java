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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationProvider {

    private final List<Verification> verifications;

    private final VerificationContextsProvider verificationContextsProvider;

    @PostConstruct
    public void init() {
        verifications.sort(Comparator.comparing(Verification::getPriority));
        log.info("active verifications:");
        verifications.forEach(verification -> log.info("{} : {}", verification.getPriority(), verification.getClass().getSimpleName()));
    }

    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {

        List<VerificationContext> possibleVerificationContexts = verificationContextsProvider
                .createPossibleVerificationContexts(layoutMetaBlock, productsToVerify);

        List<VerificationRunResult> verificationRunResults = possibleVerificationContexts
                .stream()
                .map(context -> verifications
                        .stream()
                        .map(verification -> verification.verify(context))
                        .filter(result -> !result.isRunIsValid())
                        .findFirst().orElse(VerificationRunResult
                                .builder()
                                .runIsValid(true)
                                .validLinkMetaBlocks(context.getOriginalLinkMetaBlocks())
                                .build())
                ).collect(Collectors.toList());

        return verificationRunResults
                .stream()
                .map(verificationRunResult -> {
                    log.info("context validity: {}", verificationRunResult.isRunIsValid());
                    return verificationRunResult;
                })
                .filter(VerificationRunResult::isRunIsValid)
                .findFirst().orElse(VerificationRunResult.valid(false));
    }
}
