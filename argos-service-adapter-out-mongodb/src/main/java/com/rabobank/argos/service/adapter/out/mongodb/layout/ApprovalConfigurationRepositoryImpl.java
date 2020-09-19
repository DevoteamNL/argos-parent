/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.out.mongodb.layout;

import com.rabobank.argos.domain.layout.ApprovalConfiguration;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
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
