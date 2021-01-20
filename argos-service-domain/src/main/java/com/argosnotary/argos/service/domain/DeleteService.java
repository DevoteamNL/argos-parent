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
package com.argosnotary.argos.service.domain;

import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.hierarchy.TreeNodeVisitor;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyService;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.layout.ReleaseConfigurationRepository;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteService implements TreeNodeVisitor<Optional<TreeNode>>{

    private final LabelRepository labelRepository;
    private final LayoutMetaBlockRepository layoutRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;
    private final ApprovalConfigurationRepository approvalConfigurationRepository;
    private final SupplyChainRepository supplyChainRepository;
    private final HierarchyService hierarchyService;
    private final AccountService accountService;
    private final ReleaseConfigurationRepository releaseConfigurationRepository;

    public void deleteLabel(String labelId) {
        hierarchyService.getSubTree(labelId, HierarchyMode.ALL, -1).ifPresent(
                treeNode -> treeNode.visit(this)
        );
    }

    public void deleteSupplyChain(String supplyChainId) {
        layoutRepository.deleteBySupplyChainId(supplyChainId);
        linkMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
        approvalConfigurationRepository.deleteBySupplyChainId(supplyChainId);
        releaseConfigurationRepository.deleteBySupplyChainId(supplyChainId);
        supplyChainRepository.delete(supplyChainId);
    }

    public void deleteServiceAccount(String serviceAccountId) {
        accountService.deleteServiceAccount(serviceAccountId);
    }

    @Override
    public boolean visitEnter(TreeNode treeNode) {
        return true;
    }

    @Override
    public void visitExit(TreeNode treeNode) {
        labelRepository.deleteById(treeNode.getReferenceId());
    }

    @Override
    public void visitLeaf(TreeNode treeNode) {
        switch (treeNode.getType()) {
        case SUPPLY_CHAIN:
            deleteSupplyChain(treeNode.getReferenceId());
            break;
        case SERVICE_ACCOUNT:
            deleteServiceAccount(treeNode.getReferenceId());
            break;
        default:
            throw new IllegalArgumentException(treeNode.getType() + " not implemented");
    }
        
    }
}
