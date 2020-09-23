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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.service.domain.account.ServiceAccountRepository;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.LabelIdExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component(ServiceAccountLabelIdExtractor.SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR)
@RequiredArgsConstructor
public class ServiceAccountLabelIdExtractor implements LabelIdExtractor {
    public static final String SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR = "ServiceAccountLabelIdExtractor";

    private final ServiceAccountRepository serviceAccountRepository;

    @Override
    public Optional<String> extractLabelId(LabelIdCheckParam checkParam, Object accountId) {
        return serviceAccountRepository.findParentLabelIdByAccountId((String) accountId);
    }
}
