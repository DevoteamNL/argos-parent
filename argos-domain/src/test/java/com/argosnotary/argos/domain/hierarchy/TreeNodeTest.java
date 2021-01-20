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
package com.argosnotary.argos.domain.hierarchy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.permission.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.argosnotary.argos.domain.hierarchy.TreeNodeTest.TestVisitor.VISIT_ENTER;
import static com.argosnotary.argos.domain.hierarchy.TreeNodeTest.TestVisitor.VISIT_EXIT;
import static com.argosnotary.argos.domain.hierarchy.TreeNodeTest.TestVisitor.VISIT_LEAF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TreeNodeTest {
    private TreeNode treeNode;
    private TreeNode leafNode;
    private TreeNode childNode;
    private TreeNode childNode2;
    
    private Permission permission = Permission.TREE_EDIT;
    private Permission permission2 = Permission.LINK_ADD;

    @BeforeEach
    void setUp() {

        leafNode = TreeNode.builder()
                .name("servicaccount")
                .type(TreeNode.Type.SERVICE_ACCOUNT)
                .build();

        childNode = TreeNode.builder()
                .name("childLabel")
                .permissions(Collections.singletonList(permission))
                .children(Collections.singletonList(leafNode))
                .type(TreeNode.Type.LABEL).build();
        
        childNode2 = TreeNode.builder()
                .name("childLabel2")
                .permissions(Collections.singletonList(permission))
                .type(TreeNode.Type.LABEL).build();
        
        List<TreeNode> children = new ArrayList<>();
        children.add(childNode);

        treeNode = TreeNode.builder()
                .name("root")
                .children(children)
                .type(TreeNode.Type.LABEL)
                .build();

    }

    @Test
    void visit() {
        TreeNodeVisitor<Map<String, Integer>> treeNodeVisitor = new TestVisitor();
        treeNode.visit(treeNodeVisitor);
        assertThat(treeNodeVisitor.result().get(VISIT_ENTER), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_EXIT), is(2));
        assertThat(treeNodeVisitor.result().get(VISIT_LEAF), is(1));
    }
    
    @Test
    void typeTest() {
        assertThat(treeNode.isLeafNode(), is(false));
        assertThat(childNode.isLeafNode(), is(false));
        assertThat(leafNode.isLeafNode(), is(true));
        
        treeNode.addChild(childNode2);
        assertThat(treeNode.getChildren().size(), is(2));
        
    }

    static class TestVisitor implements TreeNodeVisitor<Map<String, Integer>> {
        protected static final String VISIT_ENTER = "visitEnter";
        protected static final String VISIT_EXIT = "visitExit";
        protected static final String VISIT_LEAF = "visitLeaf";
        private Map<String, Integer> visits = new HashMap<>();

        public TestVisitor() {
            visits.put(VISIT_ENTER, 0);
            visits.put(VISIT_EXIT, 0);
            visits.put(VISIT_LEAF, 0);
        }

        @Override
        public boolean visitEnter(TreeNode treeNode) {
            visits.put(VISIT_ENTER, visits.get(VISIT_ENTER) + 1);
            return true;
        }

        @Override
        public void visitExit(TreeNode treeNode) {
            visits.put(VISIT_EXIT, visits.get(VISIT_EXIT) + 1);
        }

        @Override
        public void visitLeaf(TreeNode treeNode) {
            visits.put(VISIT_LEAF, visits.get(VISIT_LEAF) + 1);
        }

        @Override
        public Map<String, Integer> result() {
            return visits;
        }
    }
    
    @Test
    void withsTest() {        
        TreeNode childNodeClone = childNode.withPermissions(Collections.singletonList(permission2));
        assertThat(childNodeClone.getPermissions(), is(Collections.singletonList(permission2)));
        assertThat(childNodeClone.getChildren(), is(childNode.getChildren()));
        assertThat(childNodeClone.getName(), is(childNode.getName()));
        assertThat(childNodeClone.getType(), is(childNode.getType()));
        
        
        TreeNode rootNodeClone = treeNode.withChildren(Collections.singletonList(leafNode));
        assertThat(rootNodeClone.getChildren(), is(Collections.singletonList(leafNode)));
        assertThat(rootNodeClone.getPermissions(), is(treeNode.getPermissions()));
        assertThat(rootNodeClone.getName(), is(treeNode.getName()));
        assertThat(rootNodeClone.getType(), is(treeNode.getType()));
        
    }
    
    @Test
    void attributeTest() {
        treeNode.addChild(childNode2);
        assertThat(treeNode.getChildren(), is(Arrays.asList(childNode, childNode2)));
        
        TreeNode theNode = TreeNode.builder().build();
        theNode.setName("childLabel");
        theNode.setPermissions(Collections.singletonList(permission));
        theNode.setChildren(Collections.singletonList(leafNode));
        theNode.setType(TreeNode.Type.LABEL);
        theNode.setReferenceId("referenceId");
        theNode.setParentLabelId("parentLabelId");
        theNode.setPathToRoot(Arrays.asList("path", "to", "root"));
        theNode.setIdPathToRoot(Arrays.asList("id1", "id2"));
        theNode.setIdsOfDescendantLabels(Arrays.asList("labels"));
        theNode.setHasChildren(true);
        

        assertThat(theNode.getChildren(), is(Collections.singletonList(leafNode)));
        assertThat(theNode.getPermissions(), is(Collections.singletonList(permission)));
        assertThat(theNode.getName(), is("childLabel"));
        assertThat(theNode.getType(), is(TreeNode.Type.LABEL));
        assertThat(theNode.getReferenceId(), is("referenceId"));
        assertThat(theNode.getParentLabelId(), is("parentLabelId"));
        assertThat(theNode.getPathToRoot(), is(Arrays.asList("path", "to", "root")));
        assertThat(theNode.getIdPathToRoot(), is(Arrays.asList("id1", "id2")));
        assertThat(theNode.getIdsOfDescendantLabels(), is(Arrays.asList("labels")));
        assertThat(theNode.isHasChildren(), is(true));
        
        
    }
}