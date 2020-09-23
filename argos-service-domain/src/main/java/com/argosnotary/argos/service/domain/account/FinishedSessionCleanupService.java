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
package com.argosnotary.argos.service.domain.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinishedSessionCleanupService {

    private static final int FIRST_RUN_IN_MILLS = 10000;
    private static final int INTERVAL_IN_MILLS = 15 * 60000;

    private final FinishedSessionRepository finishedSessionRepository;

    @Scheduled(fixedRate = INTERVAL_IN_MILLS, initialDelay = FIRST_RUN_IN_MILLS)
    public void cleanup() {
        log.debug("cleanup expired sessions");
        finishedSessionRepository.deleteExpiredSessions(new Date());
    }
}
