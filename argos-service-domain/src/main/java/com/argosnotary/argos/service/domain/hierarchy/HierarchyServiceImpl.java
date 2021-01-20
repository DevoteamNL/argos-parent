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

import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.hierarchy.TreeNodeVisitor;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HierarchyServiceImpl implements HierarchyService {

    private final HierarchyRepository hierarchyRepository;
    private final AccountSecurityContext accountSecurityContext;

    @Override
    public Optional<TreeNode> getSubTree(String referenceId, HierarchyMode hierarchyMode, Integer maxDepth) {
        TreeNodeVisitor<Optional<TreeNode>> treeNodeVisitor = new AccountPermissionTreeNodeVisitor(accountSecurityContext);
        hierarchyRepository
                .getSubTree(referenceId, hierarchyMode, maxDepth)
                .ifPresent(treeNode -> treeNode.visit(treeNodeVisitor)
                );

        return treeNodeVisitor.result();
    }

    @Override
    public List<TreeNode> getRootNodes(HierarchyMode hierarchyMode, int maxDepth) {
        return hierarchyRepository
                .getRootNodes(hierarchyMode, maxDepth)
                .stream()
                .map(treeNode -> {
                    TreeNodeVisitor<Optional<TreeNode>> treeNodeVisitor = new AccountPermissionTreeNodeVisitor(accountSecurityContext);
                            treeNode.visit(treeNodeVisitor);
                            return treeNodeVisitor.result();
                        }
                ).filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
