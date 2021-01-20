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
package com.argosnotary.argos.service.domain.security;

import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.argosnotary.argos.domain.permission.Permission.LOCAL_PERMISSION_EDIT;
import static com.argosnotary.argos.domain.permission.Permission.READ;
import static com.argosnotary.argos.domain.permission.Permission.TREE_EDIT;
import static com.argosnotary.argos.service.domain.security.DefaultLocalPermissionCheckStrategy.DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME;
import static java.util.Objects.requireNonNull;

@Component(DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME)
@RequiredArgsConstructor
@Slf4j
public class DefaultLocalPermissionCheckStrategy implements LocalPermissionCheckStrategy {

    private final HierarchyRepository hierarchyRepository;

    private final AccountSecurityContext accountSecurityContext;

    public static final String DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME = "defaultLocalPermissionCheckStrategy";
    private final Set<Permission> implicitReadPermissions = EnumSet.of(TREE_EDIT, LOCAL_PERMISSION_EDIT);

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
