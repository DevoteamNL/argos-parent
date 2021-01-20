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
package com.argosnotary.argos.service.adapter.out.mongodb.layout;

import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

import static com.argosnotary.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalConfigurationRepositoryImplTest {
    @Mock
    private MongoTemplate template;

    private ApprovalConfigurationRepositoryImpl approvalConfigurationRepository;

    @Mock
    private ApprovalConfiguration approvalConfiguration;
    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @BeforeEach
    void setup() {
        approvalConfigurationRepository = new ApprovalConfigurationRepositoryImpl(template);
    }

    @Test
    void findBySupplyChainIdSegmentNameAndStepName() {
        when(template.findOne(any(), eq(ApprovalConfiguration.class), eq(COLLECTION))).thenReturn(approvalConfiguration);
        assertThat(approvalConfigurationRepository.findBySupplyChainIdSegmentNameAndStepName("supplyChain", "segmentName", "stepName"), is(Optional.of(approvalConfiguration)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChain\", \"$and\" : [{ \"segmentName\" : \"segmentName\"}, { \"stepName\" : \"stepName\"}]}, Fields: {}, Sort: {}"));
    }


    @Test
    void findBySupplyChainId() {
        when(template.find(any(Query.class), eq(ApprovalConfiguration.class), eq(COLLECTION))).thenReturn(List.of(approvalConfiguration));
        List<ApprovalConfiguration> approvalConfigurations = approvalConfigurationRepository.findBySupplyChainId("supplyChain");
        assertThat(approvalConfigurations, hasSize(1));
        verify(template).find(queryArgumentCaptor.capture(), eq(ApprovalConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void saveAll() {
        approvalConfigurationRepository.saveAll("supplyChain", List.of(approvalConfiguration));
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        verify(template).insert(List.of(approvalConfiguration), COLLECTION);
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void deleteBySupplyChainId() {
        approvalConfigurationRepository.deleteBySupplyChainId("supplyChain");
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }
}