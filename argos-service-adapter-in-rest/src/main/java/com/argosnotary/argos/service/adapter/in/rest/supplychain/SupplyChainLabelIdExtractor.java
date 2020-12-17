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
package com.argosnotary.argos.service.adapter.in.rest.supplychain;

import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.LabelIdExtractor;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component(SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
@RequiredArgsConstructor
public class SupplyChainLabelIdExtractor implements LabelIdExtractor {
    public static final String SUPPLY_CHAIN_LABEL_ID_EXTRACTOR = "SupplyChainLabelIdExtractor";

    private final SupplyChainRepository supplyChainRepository;

    @Override
    public Optional<String> extractLabelId(LabelIdCheckParam checkParam, Object supplyChainId) {
        return supplyChainRepository.findParentLabelIdBySupplyChainId((String) supplyChainId);
    }
}
