/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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
package com.argosnotary.argos.service.adapter.in.rest.supplychain;

import com.argosnotary.argos.domain.SupplyChainHelper;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.supplychain.SupplyChain;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.SupplychainApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSupplyChain;
import com.argosnotary.argos.service.domain.DeleteService;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
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

import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;
import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainPathLocalPermissionCheckDataExtractor.SUPPLY_CHAIN_PATH_LOCAL_DATA_EXTRACTOR;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupplyChainRestService implements SupplychainApi {

    private final SupplyChainRepository supplyChainRepository;
    private final HierarchyRepository hierarchyRepository;
    private final SupplyChainMapper converter;
    private final LabelRepository labelRepository;
    private final DeleteService deleteService;

    @Override
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<RestSupplyChain> createSupplyChain(@LabelIdCheckParam(propertyPath = "parentLabelId") @AuditParam("supplyChain") RestSupplyChain restSupplyChain) {
        verifyParentLabelExists(restSupplyChain.getParentLabelId());
        SupplyChain supplyChain = converter.convertFromRestSupplyChainCommand(restSupplyChain);

        supplyChainRepository.save(supplyChain);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{supplyChainId}")
                .buildAndExpand(supplyChain.getSupplyChainId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(converter.convertToRestRestSupplyChainItem(supplyChain));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestSupplyChain> getSupplyChain(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        SupplyChain supplyChain = supplyChainRepository
                .findBySupplyChainId(supplyChainId)
                .orElseThrow(() -> supplyChainNotFound(supplyChainId));
        return ResponseEntity.ok(converter.convertToRestRestSupplyChainItem(supplyChain));
    }


    @Override
    @PermissionCheck(permissions = {Permission.READ, Permission.LINK_ADD, Permission.VERIFY}, localPermissionDataExtractorBean = SUPPLY_CHAIN_PATH_LOCAL_DATA_EXTRACTOR)
    public ResponseEntity<RestSupplyChain> getSupplyChainByPath(String name, List<String> path) {
        List<String> pathToRoot = SupplyChainHelper.reversePath(path);
        return hierarchyRepository.findByNamePathToRootAndType(name, pathToRoot, TreeNode.Type.SUPPLY_CHAIN)
                .map(TreeNode::getReferenceId)
                .flatMap(supplyChainRepository::findBySupplyChainId)
                .map(converter::convertToRestRestSupplyChainItem)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> supplyChainNotFound(name, path));
    }

    @Override
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<RestSupplyChain> updateSupplyChain(
            @LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
            @AuditParam("supplyChainId")
                    String supplyChainId,
            @LabelIdCheckParam(propertyPath = "parentLabelId")
            @AuditParam("supplyChain")
                    RestSupplyChain restSupplyChain) {
        verifyParentLabelExists(restSupplyChain.getParentLabelId());
        SupplyChain supplyChain = converter.convertFromRestSupplyChainCommand(restSupplyChain);
        supplyChain.setSupplyChainId(supplyChainId);
        return supplyChainRepository.update(supplyChainId, supplyChain)
                .map(converter::convertToRestRestSupplyChainItem)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> supplyChainNotFound(supplyChainId));
    }

    @Override
    @PermissionCheck(permissions = Permission.TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<Void> deleteSupplyChainById(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
                                                      @AuditParam("supplyChainId") String supplyChainId) {
        if (supplyChainRepository.exists(supplyChainId)) {
            deleteService.deleteSupplyChain(supplyChainId);
            return ResponseEntity.noContent().build();
        } else {
            throw supplyChainNotFound(supplyChainId);
        }
    }

    private void verifyParentLabelExists(String parentLabelId) {
        if (!labelRepository.exists(parentLabelId)) {
            throw parentLabelNotFound(parentLabelId);
        }
    }

    private ResponseStatusException parentLabelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent label not found : " + labelId);
    }

    private ResponseStatusException supplyChainNotFound(String supplyChainId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
    }

    private ResponseStatusException supplyChainNotFound(String supplyChainName, List<String> pathToRoot) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainName + " with path " + String.join(",", pathToRoot));
    }

}
