/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
package com.argosnotary.argos.service.adapter.in.rest.supplychain;

import com.argosnotary.argos.domain.SupplyChainHelper;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.security.LocalPermissionCheckData;
import com.argosnotary.argos.service.domain.security.LocalPermissionCheckDataExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.emptySet;

@Component(SupplyChainPathLocalPermissionCheckDataExtractor.SUPPLY_CHAIN_PATH_LOCAL_DATA_EXTRACTOR)
@RequiredArgsConstructor
public class SupplyChainPathLocalPermissionCheckDataExtractor implements LocalPermissionCheckDataExtractor {
    public static final String SUPPLY_CHAIN_PATH_LOCAL_DATA_EXTRACTOR = "SupplyChainPathLocalPermissionCheckDataExtractor";

    private final HierarchyRepository hierarchyRepository;

    @Override
    public LocalPermissionCheckData extractLocalPermissionCheckData(Method method, Object[] argumentValues) {
        List<String> pathToRoot = SupplyChainHelper.reversePath((List<String>) argumentValues[1]);
        return hierarchyRepository
                .findByNamePathToRootAndType((String) argumentValues[0], pathToRoot, TreeNode.Type.SUPPLY_CHAIN)
                .map(TreeNode::getParentLabelId)
                .map(parentLabelId -> LocalPermissionCheckData
                        .builder()
                        .labelIds(new HashSet<>(List.of(parentLabelId)))
                        .build())
                .orElse(LocalPermissionCheckData
                        .builder()
                        .labelIds(emptySet())
                        .build());
    }
}
