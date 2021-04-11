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
package com.argosnotary.argos.service.adapter.out.mongodb.link;

import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl.COLLECTION;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkMetaBlockRepositoryImplTest {


    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SHA = "sha";

    @Mock
    private MongoTemplate template;

    @Mock
    private LinkMetaBlock link;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private LinkMetaBlockRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new LinkMetaBlockRepositoryImpl(template);
    }

    @Test
    void findBySupplyChainId() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findBySupplyChainAndSha() {
        when(template.find(any(), eq(LinkMetaBlock.class), eq(COLLECTION))).thenReturn(singletonList(linkMetaBlock));
        List<LinkMetaBlock> blocks = repository.findBySupplyChainAndSha(SUPPLY_CHAIN_ID, SHA);
        assertThat(blocks, hasSize(1));
        assertThat(blocks.get(0), sameInstance(linkMetaBlock));
        verify(template).find(queryArgumentCaptor.capture(), eq(LinkMetaBlock.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\", \"$and\" : [{ \"$or\" : [{ \"link.materials.hash\" : \"sha\"}, { \"link.products.hash\" : \"sha\"}]}]}, Fields: {}, Sort: {}"));
    }
    
    @Test
    void deleteBySupplyChainId() {
        repository.deleteBySupplyChainId("supplyChainId");
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"supplyChainId\" : \"supplyChainId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void save() {
        repository.save(link);
        verify(template).save(link, COLLECTION);
    }
}
