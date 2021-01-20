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
package com.argosnotary.argos.service.domain.hierarchy;

import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.hierarchy.TreeNodeVisitor;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.argosnotary.argos.domain.permission.Permission.LINK_ADD;
import static com.argosnotary.argos.domain.permission.Permission.LOCAL_PERMISSION_EDIT;
import static com.argosnotary.argos.domain.permission.Permission.READ;
import static com.argosnotary.argos.domain.permission.Permission.RELEASE;
import static com.argosnotary.argos.domain.permission.Permission.TREE_EDIT;

public class AccountPermissionTreeNodeVisitor implements TreeNodeVisitor<Optional<TreeNode>> {

    private TreeNode treeNodeWithUserPermissions;
    private HashMap<String, TreeNode> parentRegistry = new HashMap<>();
    private final AccountSecurityContext accountSecurityContext;
    private Set<Permission> hierarchyPermissions = EnumSet.of(READ, TREE_EDIT, LOCAL_PERMISSION_EDIT, LINK_ADD, RELEASE);

    AccountPermissionTreeNodeVisitor(final AccountSecurityContext accountSecurityContext) {
        this.accountSecurityContext = accountSecurityContext;
    }

    @Override
    public boolean visitEnter(TreeNode treeNode) {
        TreeNode copyOfTreeNode = treeNode
                .withChildren(new ArrayList<>())
                .withPermissions(determineAggregatedPermissions(treeNode));

        if (!copyOfTreeNode.getPermissions().isEmpty() || !nodeHasNoPermissionsUpTree(treeNode.getIdsOfDescendantLabels())) {
            if (treeNodeWithUserPermissions == null) {
                treeNodeWithUserPermissions = copyOfTreeNode;
    
            } else {
    
                TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
                parent.addChild(copyOfTreeNode);
            }
    
            parentRegistry.put(copyOfTreeNode.getReferenceId(), copyOfTreeNode);
            return true;
        }
        return false;
    }

    private boolean nodeHasNoPermissionsUpTree(List<String> idsOfDescendantLabels) {

        return accountSecurityContext
                .allLocalPermissions(idsOfDescendantLabels)
                .isEmpty();
    }

    private List<Permission> determineAggregatedPermissions(TreeNode treeNode) {
        Set<Permission> aggregatedPermissions = new HashSet<>();
        List<String> labelIdsDownTree = new ArrayList<>(treeNode.getIdPathToRoot());
        if (!treeNode.isLeafNode()) {
            labelIdsDownTree.add(treeNode.getReferenceId());
        }
        aggregatedPermissions.addAll(accountSecurityContext.allLocalPermissions(labelIdsDownTree));
        aggregatedPermissions.addAll(accountSecurityContext.getGlobalPermission());
        Set<Permission> hierarchyOnlyPermissions = filterForHierarchyOnlyPermissions(aggregatedPermissions);
        List<Permission> arrayOfHierarchyOnlyPermissions = new ArrayList<>(hierarchyOnlyPermissions);
        arrayOfHierarchyOnlyPermissions.sort(Comparator.comparing(Permission::name));
        return arrayOfHierarchyOnlyPermissions;
    }

    private Set<Permission> filterForHierarchyOnlyPermissions(Set<Permission> aggregatedPermissions) {
       return  aggregatedPermissions
                .stream()
                .filter(permission-> hierarchyPermissions.contains(permission))
                .collect(Collectors.toSet());
    }

    @Override
    public void visitExit(TreeNode treeNode) {
        // do nothing on exit of node
    }

    @Override
    public void visitLeaf(TreeNode treeNode) {

        TreeNode copyOfTreeNode = treeNode.withPermissions(determineAggregatedPermissions(treeNode));
        if (!copyOfTreeNode.getPermissions().isEmpty()) {
            if (treeNodeWithUserPermissions == null) {
                treeNodeWithUserPermissions = copyOfTreeNode;
            }
    
            if (parentRegistry.containsKey(copyOfTreeNode.getParentLabelId())) {
                TreeNode parent = parentRegistry.get(copyOfTreeNode.getParentLabelId());
                parent.addChild(copyOfTreeNode);
            }
        }
    }

    @Override
    public Optional<TreeNode> result() {
        return Optional.ofNullable(treeNodeWithUserPermissions);
    }
}
