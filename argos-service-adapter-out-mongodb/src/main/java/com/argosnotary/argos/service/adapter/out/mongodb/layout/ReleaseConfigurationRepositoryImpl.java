/*
 * Copyright (C) 2020 Argos Notary
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
