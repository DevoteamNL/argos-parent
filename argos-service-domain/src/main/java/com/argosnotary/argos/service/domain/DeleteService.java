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
