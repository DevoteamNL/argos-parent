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

import com.argosnotary.argos.domain.permission.Permission;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
public class TreeNode {
    public enum Type {
        LABEL(false), SUPPLY_CHAIN(true), SERVICE_ACCOUNT(true);
        private boolean isLeafNode;

        Type(boolean isLeafNode) {
            this.isLeafNode = isLeafNode;
        }

        boolean isLeafNode() {
            return this.isLeafNode;
        }
    }

    private String referenceId;
    private String name;
    private String parentLabelId;
    @With
    @Builder.Default
    private List<TreeNode> children = new ArrayList<>();
    private Type type;
    private List<String> pathToRoot;
    private List<String> idPathToRoot;
    private List<String> idsOfDescendantLabels;
    private boolean hasChildren;

    @With
    private List<Permission> permissions;

    public void visit(TreeNodeVisitor<?> treeNodeVisitor) {
        if (isLeafNode()) {
            treeNodeVisitor.visitLeaf(this);
        } else if (treeNodeVisitor.visitEnter(this)) {
            children.forEach(child -> child.visit(treeNodeVisitor));
            treeNodeVisitor.visitExit(this);
        }
    }

    public boolean isLeafNode() {
        return type.isLeafNode();
    }

    public void addChild(TreeNode treeNode) {
        children.add(treeNode);
    }
}
