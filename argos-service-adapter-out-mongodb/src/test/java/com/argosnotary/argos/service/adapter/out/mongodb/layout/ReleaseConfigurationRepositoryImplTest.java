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