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
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class SupplyChainRepositoryImpl implements SupplyChainRepository {

    public static final String COLLECTION = "supplyChains";
    public static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    public static final String SUPPLY_CHAIN_NAME_FIELD = "name";
    public static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    private final MongoTemplate template;

    @Override
    public void save(SupplyChain supplyChain) {
        try {
            template.save(supplyChain, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(supplyChain, e);
        }
    }

    @Override
    public Optional<SupplyChain> findBySupplyChainId(String supplyChainId) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION));
    }

    @Override
    public Optional<SupplyChain> update(String supplyChainId, SupplyChain supplyChain) {
        Query query = getPrimaryKeyQuery(supplyChainId);
        Document document = new Document();
        template.getConverter().write(supplyChain, document);
        try {
            UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), SupplyChain.class, COLLECTION);
            if (updateResult.getMatchedCount() > 0) {
                supplyChain.setSupplyChainId(supplyChainId);
                return Optional.of(supplyChain);
            } else {
                return Optional.empty();
            }
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(supplyChain, e);
        }
    }

    @Override
    public boolean exists(String supplyChainId) {
        return template.exists(getPrimaryKeyQuery(supplyChainId), SupplyChain.class, COLLECTION);
    }

    @Override
    public Optional<String> findParentLabelIdBySupplyChainId(String supplyChainId) {
        Query query = getPrimaryKeyQuery(supplyChainId);
        query.fields().include(PARENT_LABEL_ID_FIELD);
        return Optional.ofNullable(template.findOne(query, SupplyChain.class, COLLECTION)).map(SupplyChain::getParentLabelId);
    }

    @Override
    public void delete(String supplyChainId) {
        template.remove(getPrimaryKeyQuery(supplyChainId), COLLECTION);
    }

    private Query getPrimaryKeyQuery(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
    }

    private ArgosError duplicateKeyException(SupplyChain supplyChain, DuplicateKeyException e) {
        return new ArgosError("supply chain with name: " + supplyChain.getName() + " and parentLabelId: " + supplyChain.getParentLabelId() + " already exists", e, ArgosError.Level.WARNING);
    }
}
