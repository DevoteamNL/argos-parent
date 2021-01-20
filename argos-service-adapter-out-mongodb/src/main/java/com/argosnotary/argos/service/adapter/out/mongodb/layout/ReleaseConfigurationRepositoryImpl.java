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
import com.argosnotary.argos.service.domain.layout.ReleaseConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReleaseConfigurationRepositoryImpl implements ReleaseConfigurationRepository {
    public static final String COLLECTION = "releaseConfiguration";
    public static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    private final MongoTemplate template;

    @Override
    public void save(ReleaseConfiguration releaseConfiguration) {
        deleteBySupplyChainId(releaseConfiguration.getSupplyChainId());
        template.insert(releaseConfiguration, COLLECTION);
    }

    @Override
    public Optional<ReleaseConfiguration> findBySupplyChainId(String supplyChainId) {
        return Optional.ofNullable(template.findOne(bySupplyChainId(supplyChainId), ReleaseConfiguration.class, COLLECTION));
    }

    private Query bySupplyChainId(String supplyChainId) {
        return new Query(Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
    }

    @Override
    public void deleteBySupplyChainId(String supplyChainId) {
        template.remove(bySupplyChainId(supplyChainId), COLLECTION);
    }
}
