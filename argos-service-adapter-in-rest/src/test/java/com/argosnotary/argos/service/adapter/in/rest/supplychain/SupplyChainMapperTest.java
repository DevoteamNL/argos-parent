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

import com.argosnotary.argos.domain.supplychain.SupplyChain;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSupplyChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.core.Is.is;

class SupplyChainMapperTest {

    private static final String NAME = "name";
    private static final String ID = "ID";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private SupplyChainMapper supplyChainMapper;

    @BeforeEach
    public void setup() {
        supplyChainMapper = Mappers.getMapper(SupplyChainMapper.class);
    }

    @Test
    void convertFromRestSupplyChainCommand_Should_Return_SupplyChain() {
        RestSupplyChain restCreateSupplyChainCommand = new RestSupplyChain();
        restCreateSupplyChainCommand.name(NAME);
        restCreateSupplyChainCommand.setParentLabelId(PARENT_LABEL_ID);
        SupplyChain supplyChain = supplyChainMapper.convertFromRestSupplyChainCommand(restCreateSupplyChainCommand);
        assertThat(supplyChain.getName(), is(NAME));
        assertThat(supplyChain.getParentLabelId(), is(PARENT_LABEL_ID));
        assertThat(supplyChain.getSupplyChainId(), hasLength(36));
    }

    @Test
    void convertToRestRestSupplyChainItem_Should_Return_RestSupplyChainItem() {
        SupplyChain supplyChain = SupplyChain.builder().name(NAME).supplyChainId(ID).parentLabelId(PARENT_LABEL_ID).build();
        RestSupplyChain restSupplyChainItem = supplyChainMapper.convertToRestRestSupplyChainItem(supplyChain);
        assertThat(restSupplyChainItem.getName(), is(NAME));
        assertThat(restSupplyChainItem.getId(), is(ID));
        assertThat(restSupplyChainItem.getParentLabelId(), is(PARENT_LABEL_ID));
    }
}
