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
