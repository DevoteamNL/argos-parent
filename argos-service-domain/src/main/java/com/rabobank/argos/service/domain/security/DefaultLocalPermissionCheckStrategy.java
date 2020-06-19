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
package com.rabobank.argos.service.domain.security;

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.rabobank.argos.domain.permission.Permission.LOCAL_PERMISSION_EDIT;
import static com.rabobank.argos.domain.permission.Permission.READ;
import static com.rabobank.argos.domain.permission.Permission.SERVICE_ACCOUNT_EDIT;
import static com.rabobank.argos.domain.permission.Permission.TREE_EDIT;
import static com.rabobank.argos.service.domain.security.DefaultLocalPermissionCheckStrategy.DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME;
import static java.util.Objects.requireNonNull;

@Component(DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
public class DefaultLocalPermissionCheckStrategy implements LocalPermissionCheckStrategy {

    private final HierarchyRepository hierarchyRepository;

    private final AccountSecurityContext accountSecurityContext;

    public static final String DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME = "defaultLocalPermissionCheckStrategy";
    private final Set<Permission> implicitReadPermissions = EnumSet.of(TREE_EDIT, SERVICE_ACCOUNT_EDIT, LOCAL_PERMISSION_EDIT);

    @Override
    public boolean hasLocalPermission(LocalPermissionCheckData localPermissionCheckData, Set<Permission> permissionsToCheck) {
        log.info("hasLocalPermission on label {} with permissionsToCheck : {}", localPermissionCheckData, permissionsToCheck);

        if (!localPermissionCheckData.getLabelIds().isEmpty()) {
            return localPermissionCheckData.getLabelIds().stream().allMatch(labelId -> hasLocalPermission(permissionsToCheck, requireNonNull(labelId)));
        }
        return false;
    }

    private boolean hasLocalPermission(Set<Permission> permissionsToCheck, String labelId) {
        return accountSecurityContext.allLocalPermissions(getAllLabelIdsUpTree(labelId))
                .stream()
                .anyMatch(permission -> permissionsToCheck.contains(permission) || isImplicitRead(permissionsToCheck, permission));
    }

    private boolean isImplicitRead(Set<Permission> permissionsToCheck, Permission permission) {
        if (permissionsToCheck.contains(READ)) {
            return implicitReadPermissions.contains(permission);
        }
        return false;
    }

    private ArrayList<String> getAllLabelIdsUpTree(String labelId) {
       return hierarchyRepository.getSubTree(labelId, HierarchyMode.NONE, 0)
               .map(TreeNode::getIdPathToRoot).map(ArrayList::new)
               .map(labelIds -> {
                   labelIds.add(labelId);
                   return labelIds;
               }).orElse(new ArrayList<>(List.of(labelId)));
    }
}
