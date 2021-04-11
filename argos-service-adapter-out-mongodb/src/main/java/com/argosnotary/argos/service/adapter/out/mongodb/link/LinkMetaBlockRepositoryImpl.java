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

import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class LinkMetaBlockRepositoryImpl implements LinkMetaBlockRepository {

    public static final String COLLECTION = "linkMetaBlocks";
    public static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    public static final String STEP_NAME_FIELD = "link.stepName";
    public static final String LINK_MATERIALS_HASH_FIELD = "link.materials.hash";
    public static final String LINK_PRODUCTS_HASH_FIELD = "link.products.hash";
    public static final String LINK_MATERIALS_URI_FIELD = "link.materials.uri";
    public static final String LINK_PRODUCTS_URI_FIELD = "link.products.uri";

    private final MongoTemplate template;

    @Override
    public List<LinkMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)
                .andOperator(
                        new Criteria()
                                .orOperator(
                                        new Criteria(LINK_MATERIALS_HASH_FIELD).is(hash),
                                        new Criteria(LINK_PRODUCTS_HASH_FIELD).is(hash)
                                )
                )
        );

        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public void save(LinkMetaBlock link) {
        template.save(link, COLLECTION);
    }
    
    @Override
    public void deleteBySupplyChainId(String supplyChainId) {
        template.remove(new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)), COLLECTION);
    }
}
