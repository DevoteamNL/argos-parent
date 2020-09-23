/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.account.AccountInfo;
import com.argosnotary.argos.domain.account.AccountType;
import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.SearchAccountApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountInfo;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountKeyInfo;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountType;
import com.argosnotary.argos.service.domain.account.AccountInfoRepository;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountKeyInfo.KeyStatusEnum.DELETED;
import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class SearchAccountRestservice implements SearchAccountApi {

    private final HierarchyRepository hierarchyRepository;

    private final AccountInfoRepository accountInfoRepository;

    private final AccountKeyInfoMapper accountKeyInfoMapper;

    private final AccountInfoMapper accountInfoMapper;
    @PermissionCheck(permissions = Permission.READ)
    @Override
    public ResponseEntity<List<RestAccountInfo>> searchAccounts(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, 
            String name, 
            RestAccountType restAccountType) {
        Optional<TreeNode> supplyChainTreeNode = hierarchyRepository.getSubTree(supplyChainId, HierarchyMode.NONE, 0);
        List<String> idPathToRoot = supplyChainTreeNode.map(TreeNode::getIdPathToRoot)
                .orElse(Collections.emptyList());
        AccountType accountType = restAccountType != null ? AccountType.valueOf(restAccountType.name()) : null;
        List<AccountInfo> accountInfos = accountInfoRepository.findByNameIdPathToRootAndAccountType(name, idPathToRoot, accountType);
        List<RestAccountInfo> restAccountInfos = accountInfos
                .stream()
                .map(accountInfoMapper::convertToRestAccountInfo)
                .collect(Collectors.toList());

        return ResponseEntity.ok(restAccountInfos);
    }

    @PermissionCheck(permissions = Permission.READ)
    @Override
    public ResponseEntity<List<RestAccountKeyInfo>> searchKeysFromAccount(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, 
            List<String> keyIds) {
        List<RestAccountKeyInfo> restAccountKeyInfos = accountInfoRepository.findByKeyIds(keyIds)
                .stream()
                .map(accountKeyInfoMapper::convertToRestAccountKeyInfo)
                .collect(Collectors.toList());
        restAccountKeyInfos.addAll(createRemovedAccountKeyInfos(keyIds, restAccountKeyInfos));
        return ResponseEntity.ok(restAccountKeyInfos);
    }

    private List<RestAccountKeyInfo> createRemovedAccountKeyInfos(List<String> keyIds, 
            List<RestAccountKeyInfo> restAccountKeyInfos) {
        List<String> returnedKeyIds = restAccountKeyInfos
                .stream()
                .map(RestAccountKeyInfo::getKeyId)
                .collect(Collectors.toList());
        return keyIds
                .stream()
                .filter(keyId -> !returnedKeyIds.contains(keyId))
                .map(keyId -> new RestAccountKeyInfo()
                        .keyId(keyId)
                        .keyStatus(DELETED))
                .collect(Collectors.toList());
    }
}
