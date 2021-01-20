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

import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;
import com.argosnotary.argos.domain.crypto.signing.SignatureAlgorithm;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private LayoutMetaBlockRepository layoutRepository;

    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private ApprovalConfigurationRepository approvalConfigurationRepository;

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private HierarchyService hierarchyService;

    @Mock
    private AccountService accountService;

    @Mock
    private TreeNode treeNode;

    @Mock
    private TreeNode labelTreeNode;

    @Mock
    private TreeNode supplyChainTreeNode;

    @Mock
    private TreeNode serviceAccountTreeNode;

    @Mock
    private ReleaseConfigurationRepository releaseConfigurationRepository;

    private final static String ID = "id";

    private DeleteService service;

    @BeforeEach
    void setUp() {
        service = new DeleteService(labelRepository, layoutRepository, linkMetaBlockRepository, approvalConfigurationRepository, supplyChainRepository, hierarchyService, accountService, releaseConfigurationRepository);
    }
    
    @Test
    void deleteTree() {
        TreeNode saNode = TreeNode.builder()
                .name("servicaccount")
                .referenceId("servicaccountId")
                .type(TreeNode.Type.SERVICE_ACCOUNT)
                .build();
        

        TreeNode scNode = TreeNode.builder()
                .name("supplychain")
                .referenceId("supplychainId")
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .build();

        TreeNode labelNode = TreeNode.builder()
                .name("childLabel")
                .hasChildren(true)
                .referenceId("childLabelId")
                .children(Collections.singletonList(saNode))
                .type(TreeNode.Type.LABEL).build();
        
        TreeNode labelNode2 = TreeNode.builder()
                .name("childLabel2")
                .hasChildren(true)
                .referenceId("childLabel2Id")
                .children(Collections.singletonList(scNode))
                .type(TreeNode.Type.LABEL).build();
        
        List<TreeNode> children = new ArrayList<>();
        children.add(labelNode);
        children.add(labelNode2);

        TreeNode rootNode = TreeNode.builder()
                .name("root")
                .hasChildren(true)
                .referenceId("rootId")
                .children(children)
                .type(TreeNode.Type.LABEL)
                .build();
        when(hierarchyService.getSubTree(ID, HierarchyMode.ALL, -1)).thenReturn(Optional.of(rootNode));
        service.deleteLabel(ID);
        verify(labelRepository).deleteById("rootId");
        verify(labelRepository).deleteById("childLabelId");
        verify(labelRepository).deleteById("childLabel2Id");
        verify(supplyChainRepository).delete("supplychainId");
        verify(layoutRepository).deleteBySupplyChainId("supplychainId");
        verify(linkMetaBlockRepository).deleteBySupplyChainId("supplychainId");
        verify(approvalConfigurationRepository).deleteBySupplyChainId("supplychainId");
        verify(accountService).deleteServiceAccount("servicaccountId");
        verify(releaseConfigurationRepository).deleteBySupplyChainId("supplychainId");
    }

    @Test
    void deleteSupplyChain() {
        service.deleteSupplyChain(ID);
        verify(supplyChainRepository).delete(ID);
        verify(layoutRepository).deleteBySupplyChainId(ID);
        verify(linkMetaBlockRepository).deleteBySupplyChainId(ID);
        verify(approvalConfigurationRepository).deleteBySupplyChainId(ID);
        verify(releaseConfigurationRepository).deleteBySupplyChainId(ID);
    }

    @Test
    void deleteServiceAccount() {
        service.deleteServiceAccount(ID);
        verify(accountService).deleteServiceAccount(ID);
    }
    
    @Test
    void vistorDeleteServiceTest() {
        TreeNode labelNode = TreeNode.builder()
                .name("childLabel")
                .referenceId("childLabelId")
                .type(TreeNode.Type.LABEL).build();
        TreeNode saNode = TreeNode.builder()
                .name("serviceaccount")
                .referenceId("serviceaccountId")
                .type(TreeNode.Type.SERVICE_ACCOUNT)
                .build();
        TreeNode scNode = TreeNode.builder()
                .name("supplychain")
                .referenceId("supplychainId")
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .build();
        assertThat(service.visitEnter(labelNode), is(true));
        service.visitExit(labelNode);
        verify(labelRepository).deleteById("childLabelId");
        service.visitLeaf(saNode);
        verify(accountService).deleteServiceAccount("serviceaccountId");
        service.visitLeaf(scNode);
        verify(supplyChainRepository).delete("supplychainId");
        verify(layoutRepository).deleteBySupplyChainId("supplychainId");
        verify(linkMetaBlockRepository).deleteBySupplyChainId("supplychainId");
        verify(approvalConfigurationRepository).deleteBySupplyChainId("supplychainId");
        verify(releaseConfigurationRepository).deleteBySupplyChainId("supplychainId");
        
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            service.visitLeaf(labelNode);
          });
        assertEquals("LABEL not implemented", exception.getMessage());
    }
}