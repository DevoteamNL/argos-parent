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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLocalPermissionCheckStrategyTest {

    private static final String ACCOUNT_NAME = "accountName";
    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private LocalPermissionCheckData localPermissionCheckData;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    private DefaultLocalPermissionCheckStrategy strategy;

    @Mock
    private TreeNode treeNode;

    @BeforeEach
    void setUp() {
        strategy = new DefaultLocalPermissionCheckStrategy(hierarchyRepository, accountSecurityContext);
    }

    @Test
    void hasLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasImplicitReadLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.TREE_EDIT));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasMultipleLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasLocalPermissionOnParentLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(List.of(PARENT_LABEL_ID));
        when(accountSecurityContext.allLocalPermissions(List.of(PARENT_LABEL_ID, LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(true));
    }

    @Test
    void hasNoLocalPermissionOnLabel() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(Collections.singletonList(LABEL_ID))).thenReturn(Set.of(Permission.READ));
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.TREE_EDIT))), is(false));
    }

    @Test
    void hasNoLocalPermissionNoLabelId() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>());
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(false));
    }

    @Test
    void hasLocalPermissionOnLocalPermissions() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(false));
    }

    @Test
    void hasNoLocalPermissionOtherLocalPermissions() {
        when(localPermissionCheckData.getLabelIds()).thenReturn(new HashSet<>(List.of(LABEL_ID)));
        when(hierarchyRepository.getSubTree(LABEL_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.emptyList());
        when(accountSecurityContext.allLocalPermissions(any())).thenReturn(emptySet());
        assertThat(strategy.hasLocalPermission(localPermissionCheckData, new HashSet<>(List.of(Permission.READ))), is(false));
    }
}