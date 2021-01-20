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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.ServiceAccountApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestServiceAccount;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.service.domain.DeleteService;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.argosnotary.argos.domain.permission.Permission.READ;
import static com.argosnotary.argos.domain.permission.Permission.TREE_EDIT;
import static com.argosnotary.argos.service.adapter.in.rest.account.ServiceAccountLabelIdExtractor.SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ServiceAccountRestService implements ServiceAccountApi {

    private final ServiceAccountMapper accountMapper;

    private final LabelRepository labelRepository;

    private final AccountKeyPairMapper keyPairMapper;

    private final AccountService accountService;

    private final AccountSecurityContext accountSecurityContext;

    private final DeleteService deleteService;

    @Override
    @PermissionCheck(permissions = TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<RestServiceAccount> createServiceAccount(@LabelIdCheckParam(propertyPath = "parentLabelId")
                                                                   @AuditParam("serviceAccount") RestServiceAccount restServiceAccount) {
        verifyParentLabelExists(restServiceAccount.getParentLabelId());
        ServiceAccount serviceAccount = accountMapper.convertFromRestServiceAccount(restServiceAccount);
        accountService.save(serviceAccount);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serviceAccountId}")
                .buildAndExpand(serviceAccount.getAccountId())
                .toUri();
        return ResponseEntity.created(location).body(accountMapper.convertToRestServiceAccount(serviceAccount));
    }

    @Override
    @PermissionCheck(permissions = TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<RestServiceAccountKeyPair> createServiceAccountKeyById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR)
                                                                                 @AuditParam("serviceAccountId") String serviceAccountId,
                                                                                 @AuditParam("keyPair") RestServiceAccountKeyPair restKeyPair) {
        ServiceAccount updatedAccount = accountService.activateNewKey(serviceAccountId, keyPairMapper.convertFromRestKeyPair(restKeyPair))
                .orElseThrow(() -> accountNotFound(serviceAccountId));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{serviceAccountId}/key")
                .buildAndExpand(serviceAccountId)
                .toUri();
        return ResponseEntity.created(location).body(keyPairMapper.convertToRestKeyPair(((ServiceAccountKeyPair) updatedAccount.getActiveKeyPair())));
    }

    @Override
    @PermissionCheck(permissions = READ)
    public ResponseEntity<RestServiceAccountKeyPair> getServiceAccountKeyById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR) String serviceAccountId) {
        return accountService.findServiceAccountById(serviceAccountId)
                .flatMap(account -> Optional.ofNullable(account.getActiveKeyPair()))
                .map(account -> (ServiceAccountKeyPair) account)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> keyNotFound(serviceAccountId));
    }

    @Override
    @PermissionCheck(permissions = READ)
    public ResponseEntity<RestServiceAccount> getServiceAccountById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR) String serviceAccountId) {
        return accountService.findServiceAccountById(serviceAccountId)
                .map(accountMapper::convertToRestServiceAccount)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> accountNotFound(serviceAccountId));
    }

    @Override
    @PermissionCheck(permissions = TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<Void> deleteServiceAccount(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR)
                                                     @AuditParam("serviceAccountId") String serviceAccountId) {
        if (accountService.serviceAccountExists(serviceAccountId)) {
            deleteService.deleteServiceAccount(serviceAccountId);
            return ResponseEntity.noContent().build();
        } else {
            throw accountNotFound(serviceAccountId);
        }
    }

    @Override
    @PermissionCheck(permissions = TREE_EDIT)
    @AuditLog
    @Transactional
    public ResponseEntity<RestServiceAccount> updateServiceAccountById(@LabelIdCheckParam(dataExtractor = SERVICE_ACCOUNT_LABEL_ID_EXTRACTOR)
                                                                       @AuditParam("serviceAccountId") String serviceAccountId,
                                                                       @AuditParam("serviceAccount") RestServiceAccount restServiceAccount) {
        verifyParentLabelExists(restServiceAccount.getParentLabelId());
        ServiceAccount serviceAccount = accountMapper.convertFromRestServiceAccount(restServiceAccount);
        return accountService.update(serviceAccountId, serviceAccount)
                .map(accountMapper::convertToRestServiceAccount)
                .map(ResponseEntity::ok).orElseThrow(() -> accountNotFound(serviceAccountId));
    }

    @Override
    @PreAuthorize("hasRole('NONPERSONAL')")
    public ResponseEntity<RestServiceAccountKeyPair> getServiceAccountKey() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(Account::getActiveKeyPair).filter(Objects::nonNull)
                .map(keyPair -> (ServiceAccountKeyPair) keyPair)
                .map(keyPairMapper::convertToRestKeyPair)
                .map(ResponseEntity::ok).orElseThrow(this::keyNotFound);
    }

    private void verifyParentLabelExists(String parentLabelId) {
        if (!labelRepository.exists(parentLabelId)) {
            throw parentLabelNotFound(parentLabelId);
        }
    }

    private ResponseStatusException keyNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active service account key found");
    }

    private ResponseStatusException keyNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no active service account key with id : " + accountId + " found");
    }

    private ResponseStatusException accountNotFound(String accountId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "no service account with id : " + accountId + " found");
    }

    private ResponseStatusException parentLabelNotFound(String parentLabelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent label id not found : " + parentLabelId);
    }
}
