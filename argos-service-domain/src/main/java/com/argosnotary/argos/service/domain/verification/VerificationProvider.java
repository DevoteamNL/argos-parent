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
