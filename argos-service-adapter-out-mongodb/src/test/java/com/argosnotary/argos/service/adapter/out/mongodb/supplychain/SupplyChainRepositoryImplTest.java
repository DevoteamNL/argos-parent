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
package com.argosnotary.argos.service.adapter.out.mongodb.supplychain;

import com.mongodb.client.result.UpdateResult;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.supplychain.SupplyChain;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static com.argosnotary.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainRepositoryImplTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SUPPLY_CHAIN_NAME = "supplyChainName";
    private static final String PARENT_LABEL_ID = "parentLabelId";

    @Mock
    private MongoTemplate template;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private SupplyChainRepositoryImpl repository;

    @Mock
    private SupplyChain supplyChain;

    @Mock
    private DuplicateKeyException duplicateKeyException;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @BeforeEach
    void setUp() {
        repository = new SupplyChainRepositoryImpl(template);
    }

    @Test
    void findBySupplyChainId() {
        when(template.findOne(any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(supplyChain);
        assertThat(repository.findBySupplyChainId(SUPPLY_CHAIN_ID), is(Optional.of(supplyChain)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void exists() {
        when(template.exists(any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.exists(SUPPLY_CHAIN_ID), is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        repository.save(supplyChain);
        verify(template).save(supplyChain, COLLECTION);
    }

    @Test
    void saveDuplicateKeyException() {
        when(supplyChain.getName()).thenReturn(SUPPLY_CHAIN_NAME);
        when(supplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        doThrow(duplicateKeyException).when(template).save(supplyChain, COLLECTION);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.save(supplyChain));
        assertThat(argosError.getMessage(), Matchers.is("supply chain with name: supplyChainName and parentLabelId: parentLabelId already exists"));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
        assertThat(argosError.getLevel(), Matchers.is(ArgosError.Level.WARNING));
    }

    @Test
    void updateFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        Optional<SupplyChain> update = repository.update(SUPPLY_CHAIN_ID, supplyChain);
        assertThat(update, Matchers.is(Optional.of(supplyChain)));
        verify(supplyChain).setSupplyChainId(SUPPLY_CHAIN_ID);
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
        verify(converter).write(eq(supplyChain), any());
        assertThat(updateArgumentCaptor.getValue().toString(), Matchers.is("{}"));
    }

    @Test
    void updateNotFound() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(updateResult);
        when(updateResult.getMatchedCount()).thenReturn(0L);
        Optional<SupplyChain> update = repository.update(SUPPLY_CHAIN_ID, supplyChain);
        assertThat(update, Matchers.is(Optional.empty()));
    }

    @Test
    void updateDuplicateKeyException() {
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(), any(), eq(SupplyChain.class), eq(COLLECTION))).thenThrow(duplicateKeyException);
        ArgosError argosError = assertThrows(ArgosError.class, () -> repository.update(SUPPLY_CHAIN_ID, supplyChain));
        assertThat(argosError.getCause(), sameInstance(duplicateKeyException));
    }

    @Test
    void findParentLabelIdBySupplyChainId() {
        when(supplyChain.getParentLabelId()).thenReturn(PARENT_LABEL_ID);
        when(template.findOne(any(), eq(SupplyChain.class), eq(COLLECTION))).thenReturn(supplyChain);
        assertThat(repository.findParentLabelIdBySupplyChainId(SUPPLY_CHAIN_ID), is(Optional.of(PARENT_LABEL_ID)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(SupplyChain.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: { \"parentLabelId\" : 1}, Sort: {}"));
    }

    @Test
    void delete() {
        repository.delete(SUPPLY_CHAIN_ID);
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }
}
