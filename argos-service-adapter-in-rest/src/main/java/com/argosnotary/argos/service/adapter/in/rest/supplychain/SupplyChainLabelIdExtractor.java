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
