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
