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
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.argosnotary.argos.domain.permission.Permission.ASSIGN_ROLE;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountPermissionTreeNodeVisitorTest {
    private static final String SUPPLY_CHAIN = "supplyChain";
    private static final String CHILD_1_2 = "child1_2";
    private static final String CHILD_1_1 = "child1_1";
    @Mock
    private AccountSecurityContext accountSecurityContext;

    private AccountPermissionTreeNodeVisitor accountPermissionTreeNodeVisitor;
    private static final String ROOT_ID = "rootId";
    private static final String CHILD_1_1_ID = "child1_1_Id";
    private static final String CHILD_1_2_ID = "child1_2_Id";
    private TreeNode root;
    private TreeNode child1_1;
    private TreeNode child1_2;
    private TreeNode child1_3;


    /*
     * Hierarchy tree node structure created for test
     * root---child1_1---child1_2---child1_3
     */
    @BeforeEach
    void setup() {

        root = TreeNode.builder()
                .pathToRoot(emptyList())
                .idPathToRoot(emptyList())
                .type(TreeNode.Type.LABEL)
                .referenceId(ROOT_ID)
                .hasChildren(true)
                .name("root")
                .build();
        child1_1 = TreeNode.builder()
                .pathToRoot(singletonList("root"))
                .idPathToRoot(singletonList(ROOT_ID))
                .parentLabelId(ROOT_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_1_ID)
                .hasChildren(true)
                .name(CHILD_1_1)
                .build();

        child1_2 = TreeNode.builder()
                .pathToRoot(List.of(CHILD_1_1, "root"))
                .idPathToRoot(List.of(CHILD_1_1_ID, ROOT_ID))
                .parentLabelId(CHILD_1_1_ID)
                .type(TreeNode.Type.LABEL)
                .referenceId(CHILD_1_2_ID)
                .hasChildren(true)
                .name(CHILD_1_2)
                .build();

        child1_3 = TreeNode.builder()
                .pathToRoot(List.of(CHILD_1_2, CHILD_1_1, "root"))
                .idPathToRoot(List.of(CHILD_1_2_ID, CHILD_1_1_ID, ROOT_ID))
                .type(TreeNode.Type.SUPPLY_CHAIN)
                .referenceId("child1_3_Id")
                .parentLabelId(CHILD_1_2_ID)
                .hasChildren(false)
                .name(SUPPLY_CHAIN)
                .build();
        createTreeNodeHierarchy();
        accountPermissionTreeNodeVisitor = new AccountPermissionTreeNodeVisitor(accountSecurityContext);
    }

    @Test
    void visitEnter() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(Set.of(Permission.READ));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.TREE_EDIT));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(root), is(true));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(child1_1), is(true));
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(true));
        assertThat(optionalTreeNode.get().getName(), is("root"));
        assertThat(optionalTreeNode.get().getChildren(), hasSize(1));
        assertThat(optionalTreeNode.get().getChildren().iterator().next().getName(), is(CHILD_1_1));

    }

    @Test
    void visitEnterWithoutPermissionsShouldReturnFalseAndEmptyResult() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(ASSIGN_ROLE));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(root), is(false));
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(false));
    }

    @Test
    void visitEnterWithoutHierarchyPermissionsShouldReturnFalseAndEmptyResult() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        when(accountSecurityContext.getGlobalPermission()).thenReturn(emptySet());
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(root), is(false));
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(false));
    }

    /*
     * @Test void visitExit() {
     * assertThat(accountPermissionTreeNodeVisitor.visitExit(child1_3), is(true)); }
     */

    @Test
    void visitLeaf() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(Set.of(Permission.READ));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.TREE_EDIT));
        assertThat(accountPermissionTreeNodeVisitor.visitEnter(child1_2), is(true));
        accountPermissionTreeNodeVisitor.visitLeaf(child1_3);
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(true));
        assertThat(optionalTreeNode.get().getName(), is(CHILD_1_2));
        assertThat(optionalTreeNode.get().getChildren(), hasSize(1));
        assertThat(optionalTreeNode.get().getChildren().iterator().next().getName(), is(SUPPLY_CHAIN));

    }

    @Test
    void visitLeafWithoutVisitEnterShouldReturnLeaf() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(Set.of(Permission.READ));
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.TREE_EDIT));
        accountPermissionTreeNodeVisitor.visitLeaf(child1_3);
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(true));
        assertThat(optionalTreeNode.get().getName(), is(SUPPLY_CHAIN));
    }

    @Test
    void visitLeafWithoutPermissionsShouldReturnFalseAndEmptyResult() {
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        when(accountSecurityContext.getGlobalPermission()).thenReturn(emptySet());
        accountPermissionTreeNodeVisitor.visitLeaf(child1_3);
        Optional<TreeNode> optionalTreeNode = accountPermissionTreeNodeVisitor.result();
        assertThat(optionalTreeNode.isPresent(), is(false));
    }

    private void createTreeNodeHierarchy() {
        root.addChild(child1_1);
        child1_1.addChild(child1_2);
        child1_2.addChild(child1_3);
    }
}