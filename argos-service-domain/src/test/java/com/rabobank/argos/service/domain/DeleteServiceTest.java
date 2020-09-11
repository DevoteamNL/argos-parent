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
package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.crypto.HashAlgorithm;
import com.rabobank.argos.domain.crypto.KeyAlgorithm;
import com.rabobank.argos.domain.crypto.signing.SignatureAlgorithm;
import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.hierarchy.TreeNodeVisitor;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.hierarchy.HierarchyService;
import com.rabobank.argos.service.domain.hierarchy.LabelRepository;
import com.rabobank.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.layout.ReleaseConfigurationRepository;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
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