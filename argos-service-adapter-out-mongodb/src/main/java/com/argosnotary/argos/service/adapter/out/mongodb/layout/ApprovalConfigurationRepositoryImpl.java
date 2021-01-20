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
import com.argosnotary.argos.service.domain.layout.ApprovalConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApprovalConfigurationRepositoryImpl implements ApprovalConfigurationRepository {
    public static final String COLLECTION = "approvalConfigurations";
    public static final String SUPPLYCHAIN_ID_FIELD = "supplyChainId";
    public static final String SEGMENT_NAME_FIELD = "segmentName";
    public static final String STEP_NAME_FIELD = "stepName";
    private final MongoTemplate template;

    @Override
    public void saveAll(String supplyChainId, List<ApprovalConfiguration> approvalConfigurations) {
        deleteBySupplyChainId(supplyChainId);
        template.insert(approvalConfigurations, COLLECTION);
    }

    public Optional<ApprovalConfiguration> findBySupplyChainIdSegmentNameAndStepName(String supplyChainId, String segmentName, String stepName) {
        Criteria criteria = Criteria.where(SUPPLYCHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        criteria.andOperator(andCriteria.toArray(new Criteria[0]));
        Query query = new Query(criteria);
        return Optional.ofNullable(template.findOne(query, ApprovalConfiguration.class, COLLECTION));
    }

    @Override
    public List<ApprovalConfiguration> findBySupplyChainId(String supplyChainId) {
        return template.find(bySupplyChainId(supplyChainId), ApprovalConfiguration.class, COLLECTION);
    }

    private Query bySupplyChainId(String supplyChainId) {
        return new Query(Criteria.where(SUPPLYCHAIN_ID_FIELD).is(supplyChainId));
    }

    @Override
    public void deleteBySupplyChainId(String supplyChainId) {
        template.remove(bySupplyChainId(supplyChainId), COLLECTION);
    }

}
