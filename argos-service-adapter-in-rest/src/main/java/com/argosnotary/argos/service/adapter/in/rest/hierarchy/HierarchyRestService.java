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
package com.argosnotary.argos.service.adapter.in.rest.hierarchy;

import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.Label;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.HierarchyApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestHierarchyMode;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLabel;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestTreeNode;
import com.argosnotary.argos.service.domain.DeleteService;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyService;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class HierarchyRestService implements HierarchyApi {

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    private final HierarchyService hierarchyService;

    private final TreeNodeMapper treeNodeMapper;

    private final DeleteService deleteService;

    @Override
    @AuditLog
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    @Transactional
    public ResponseEntity<RestLabel> createLabel(@LabelIdCheckParam(propertyPath = "parentLabelId") @AuditParam("label") RestLabel restLabel) {
        verifyParentLabelExists(restLabel.getParentLabelId());
        Label label = labelMapper.convertFromRestLabel(restLabel);
        labelRepository.save(label);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{labelId}")
                .buildAndExpand(label.getLabelId())
                .toUri();
        return ResponseEntity.created(location).body(labelMapper.convertToRestLabel(label));
    }

    private void verifyParentLabelExists(String parentLabelId) {
        Optional.ofNullable(parentLabelId)
                .filter(parentId -> !labelRepository.exists(parentId))
                .ifPresent(parentId -> {
            throw labelNotFound(parentId);
        });
    }

    @Override
    @AuditLog
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    @Transactional
    public ResponseEntity<Void> deleteLabelById(@LabelIdCheckParam @AuditParam("labelId") String labelId) {
        if (labelRepository.exists(labelId)) {
            deleteService.deleteLabel(labelId);
            return ResponseEntity.noContent().build();
        } else {
            throw labelNotFound(labelId);
        }
    }


    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestLabel> getLabelById(@LabelIdCheckParam String labelId) {
        return labelRepository.findById(labelId).map(labelMapper::convertToRestLabel).map(ResponseEntity::ok)
                .orElseThrow(() -> labelNotFound(labelId));
    }


    @Override
    @AuditLog
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    @Transactional
    public ResponseEntity<RestLabel> updateLabelById(@LabelIdCheckParam @AuditParam("labelId") String labelId, @LabelIdCheckParam(propertyPath = "parentLabelId") @AuditParam("label") RestLabel restLabel) {
        verifyParentLabelIsDifferent(labelId, restLabel.getParentLabelId());
        verifyParentLabelExists(restLabel.getParentLabelId());
        Label label = labelMapper.convertFromRestLabel(restLabel);
        label.setLabelId(labelId);
        return labelRepository.update(labelId, label)
                .map(labelMapper::convertToRestLabel).map(ResponseEntity::ok)
                .orElseThrow(() -> labelNotFound(labelId));
    }

    @Override
    public ResponseEntity<List<RestTreeNode>> getRootNodes(RestHierarchyMode hierarchyMode, Integer maxDepth) {
        return ResponseEntity.ok(hierarchyService.getRootNodes(HierarchyMode.valueOf(hierarchyMode.name()), maxDepth).stream()
                .map(treeNodeMapper::convertToRestTreeNode).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<RestTreeNode> getSubTree(String referenceId, RestHierarchyMode hierarchyMode, Integer maxDepth) {
        return hierarchyService.getSubTree(referenceId, HierarchyMode.valueOf(hierarchyMode.name()), maxDepth)
                .map(treeNodeMapper::convertToRestTreeNode).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "subtree with referenceId: " + referenceId + " not found"));
    }

    private void verifyParentLabelIsDifferent(String labelId, String parentLabelId) {
        if (labelId.equals(parentLabelId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "labelId and parentLabelId are equal");
        }
    }

    private ResponseStatusException labelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "label not found : " + labelId);
    }
}
