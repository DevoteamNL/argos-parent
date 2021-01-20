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

import com.mongodb.client.result.UpdateResult;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static com.argosnotary.argos.service.adapter.out.mongodb.layout.LayoutMetaBlockRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutMetaBlockRepositoryImplTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private MongoTemplate template;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    private LayoutMetaBlockRepositoryImpl repository;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @BeforeEach
    void setUp() {
        repository = new LayoutMetaBlockRepositoryImpl(template);
    }

    @Test
    void save() {
        repository.createOrUpdate(layoutMetaBlock);
        verify(template).save(layoutMetaBlock, COLLECTION);
    }


    @Test
    void findBySupplyChainId() {
        when(template.findOne(any(Query.class), eq(LayoutMetaBlock.class), eq(COLLECTION))).thenReturn(layoutMetaBlock);
        assertThat(repository.findBySupplyChainId(SUPPLY_CHAIN_ID), is(Optional.of(layoutMetaBlock)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(LayoutMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void update() {
        when(template.findOne(any(Query.class), eq(LayoutMetaBlock.class), eq(COLLECTION))).thenReturn(layoutMetaBlock);
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(Query.class), any(Update.class), eq(LayoutMetaBlock.class), eq(COLLECTION))).thenReturn(updateResult);
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        repository.createOrUpdate(layoutMetaBlock);

        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(LayoutMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
        verify(converter).write(eq(layoutMetaBlock), any(Document.class));
    }

    @Test
    void deleteBySupplyChainId() {
        repository.deleteBySupplyChainId(SUPPLY_CHAIN_ID);
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }
}
