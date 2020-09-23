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
package com.argosnotary.argos.service.adapter.out.mongodb.layout;

import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
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

import java.util.Optional;

import static com.argosnotary.argos.service.adapter.out.mongodb.layout.ReleaseConfigurationRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseConfigurationRepositoryImplTest {
    @Mock
    private MongoTemplate template;

    private ReleaseConfigurationRepositoryImpl releaseConfigurationRepository;

    @Mock
    private ReleaseConfiguration releaseConfiguration;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @BeforeEach
    void setup() {
        releaseConfigurationRepository = new ReleaseConfigurationRepositoryImpl(template);
    }


    @Test
    void findBySupplyChainId() {
        when(template.findOne(any(Query.class), eq(ReleaseConfiguration.class), eq(COLLECTION))).thenReturn(releaseConfiguration);
        Optional<ReleaseConfiguration> releaseConfigurationOpt = releaseConfigurationRepository.findBySupplyChainId("supplyChain");
        assertThat(releaseConfigurationOpt, is(Optional.of(releaseConfiguration)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(ReleaseConfiguration.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        when(releaseConfiguration.getSupplyChainId()).thenReturn("supplyChain");
        releaseConfigurationRepository.save(releaseConfiguration);
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        verify(template).insert(releaseConfiguration, COLLECTION);
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void deleteBySupplyChainId() {
        releaseConfigurationRepository.deleteBySupplyChainId("supplyChain");
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChain\"}, Fields: {}, Sort: {}"));
    }
}