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
package com.argosnotary.argos.service.adapter.in.rest.supplychain;

import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.security.LocalPermissionCheckData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyChainPathLocalPermissionCheckDataExtractorTest {

    private static final String OBJECT_VALUE = "value";
    private static final String LABEL_ID = "labelId";
    @Mock
    private HierarchyRepository hierarchyRepository;

    @Mock
    private TreeNode treeNode;

    private SupplyChainPathLocalPermissionCheckDataExtractor extractor;

    @Mock
    private Method method;

    @BeforeEach
    void setUp() {
        extractor = new SupplyChainPathLocalPermissionCheckDataExtractor(hierarchyRepository);
    }

    @Test
    void extractLocalPermissionCheckData() {
        when(treeNode.getParentLabelId()).thenReturn(LABEL_ID);
        when(hierarchyRepository.findByNamePathToRootAndType(OBJECT_VALUE, List.of(OBJECT_VALUE), TreeNode.Type.SUPPLY_CHAIN)).thenReturn(Optional.of(treeNode));
        LocalPermissionCheckData checkData = extractor.extractLocalPermissionCheckData(method, new Object[]{OBJECT_VALUE, List.of(OBJECT_VALUE)});
        assertThat(checkData.getLabelIds(), contains(LABEL_ID));
    }
}