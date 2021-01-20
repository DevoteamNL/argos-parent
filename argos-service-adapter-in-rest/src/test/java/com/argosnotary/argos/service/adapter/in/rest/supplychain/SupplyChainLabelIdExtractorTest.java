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
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainLabelIdExtractorTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String LABEL_ID = "labelId";

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private LabelIdCheckParam checkParam;

    private SupplyChainLabelIdExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new SupplyChainLabelIdExtractor(supplyChainRepository);
    }

    @Test
    void extractLabelId() {
        when(supplyChainRepository.findParentLabelIdBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(LABEL_ID));
        assertThat(extractor.extractLabelId(checkParam, SUPPLY_CHAIN_ID), is(Optional.of(LABEL_ID)));
    }
}