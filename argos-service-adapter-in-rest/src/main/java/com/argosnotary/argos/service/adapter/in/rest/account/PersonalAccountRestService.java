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


import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.ArgosSession;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.KeyIdProvider;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.PersonalAccountApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestProfile;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPublicKey;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRole;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestToken;
import com.argosnotary.argos.service.domain.account.AccountSearchParams;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.account.FinishedSessionRepository;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import com.argosnotary.argos.service.domain.security.TokenInfo;
import com.argosnotary.argos.service.domain.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.argosnotary.argos.domain.ArgosError.Level.WARNING;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PersonalAccountRestService implements PersonalAccountApi {

    private final AccountSecurityContext accountSecurityContext;
    private final AccountKeyPairMapper keyPairMapper;
    private final AccountService accountService;
    private final PersonalAccountMapper personalAccountMapper;
    private final LabelRepository labelRepository;
    private final FinishedSessionRepository finishedSessionRepository;
    private final TokenProvider tokenProvider;


    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<RestProfile> getPersonalAccountOfAuthenticatedUser() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(account -> (PersonalAccount) account)
                .map(personalAccountMapper::convertToRestProfile)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public ResponseEntity<Void> logout() {
        accountSecurityContext.getTokenInfo()
                .ifPresent(tokenInfo -> finishedSessionRepository.save(new ArgosSession(tokenInfo.getSessionId(), tokenInfo.getExpiration())));
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestToken> refreshToken() {
        TokenInfo tokenInfo = accountSecurityContext.getTokenInfo().orElseThrow(() -> new ArgosError("no token info"));
        return tokenProvider.refreshToken(tokenInfo).map(token -> ResponseEntity.ok(new RestToken().token(token))).orElseThrow(() -> new ArgosError("expired", WARNING));
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    @AuditLog
    @Transactional
    public ResponseEntity<Void> createKey(@AuditParam("keyPair") RestKeyPair restKeyPair) {
        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(this::accountNotFound);
        KeyPair keyPair = keyPairMapper.convertFromRestKeyPair(restKeyPair);
        validateKeyId(keyPair);
        accountService.activateNewKey(account.getAccountId(), keyPair);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PermissionCheck(permissions = {Permission.ASSIGN_ROLE})
    public ResponseEntity<RestPersonalAccount> getPersonalAccountById(String accountId) {
        return accountService.getPersonalAccountById(accountId)
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestPublicKey> getPersonalAccountKeyById(String accountId) {
        PersonalAccount account = accountService.getPersonalAccountById(accountId).orElseThrow(this::accountNotFound);
        return ResponseEntity.ok(Optional.ofNullable(account.getActiveKeyPair()).map(keyPairMapper::convertToRestPublicKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no active keypair found for account: " + account.getName())));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RestPersonalAccount>> searchPersonalAccounts(String localPermissionsLabelId, String name, List<String> activeKeyIds, List<String> inActiveKeyIds) {
        return ResponseEntity.ok(accountService.searchPersonalAccounts(AccountSearchParams.builder()
                .localPermissionsLabelId(localPermissionsLabelId)
                .name(name)
                .activeKeyIds(activeKeyIds)
                .inActiveKeyIds(inActiveKeyIds)
                .build()).stream()
                .map(personalAccountMapper::convertToRestPersonalAccountWithoutRoles).collect(Collectors.toList()));
    }
    
    @Override
    @PermissionCheck(permissions = {Permission.ASSIGN_ROLE})
    public ResponseEntity<List<RestPersonalAccount>> searchPersonalAccountsWithRoles(RestRole role, String name) {
        return ResponseEntity.ok(accountService.searchPersonalAccountsWithRoles(AccountSearchParams.builder()
                .role(role == null ? null : Role.valueOf(role.name()))
                .name(name)
                .build()).stream()
                .map(personalAccountMapper::convertToRestPersonalAccount).collect(Collectors.toList()));
    }

    @Override
    @PermissionCheck(permissions = {Permission.ASSIGN_ROLE})
    @AuditLog
    @Transactional
    public ResponseEntity<RestPersonalAccount> updatePersonalAccountRolesById(@AuditParam("accountId") String accountId, @AuditParam("roleNames") List<RestRole> roles) {
        return accountService.updatePersonalAccountRolesById(accountId, personalAccountMapper.convertToRoles(roles))
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::accountNotFound);
    }

    @Override
    @PermissionCheck(permissions = {Permission.LOCAL_PERMISSION_EDIT})
    public ResponseEntity<List<RestLocalPermissions>> getAllLocalPermissions(String accountId) {
        Set<LocalPermissions> localPermissions = accountService.getPersonalAccountById(accountId).map(PersonalAccount::getLocalPermissions).orElse(Collections.emptySet());
        return ResponseEntity.ok(localPermissions.stream().map(personalAccountMapper::convertToRestLocalPermissions).collect(Collectors.toList()));
    }

    @Override
    @PermissionCheck(permissions = {Permission.LOCAL_PERMISSION_EDIT})
    public ResponseEntity<RestLocalPermissions> getLocalPermissionsForLabel(String accountId, @LabelIdCheckParam String labelId) {
        PersonalAccount personalAccount = accountService.getPersonalAccountById(accountId).orElseThrow(this::accountNotFound);
        return ResponseEntity.ok(personalAccount.getLocalPermissions().stream()
                .filter(localPermissions -> localPermissions.getLabelId().equals(labelId))
                .findFirst().map(personalAccountMapper::convertToRestLocalPermissions)
                .orElseGet(() -> new RestLocalPermissions().labelId(labelId)));
    }

    @Override
    @PermissionCheck(permissions = {Permission.LOCAL_PERMISSION_EDIT})
    @AuditLog
    @Transactional
    public ResponseEntity<RestLocalPermissions> updateLocalPermissionsForLabel(@AuditParam("accountId") String accountId,
                                                                               @LabelIdCheckParam @AuditParam("labelId") String labelId,
                                                                               @AuditParam("localPermissions") List<RestPermission> restPermissions) {
        verifyParentLabelExists(labelId);
        LocalPermissions newLocalPermissions = LocalPermissions.builder().labelId(labelId).permissions(personalAccountMapper.convertToPermissionSet(restPermissions).stream().collect(Collectors.toSet())).build();
        PersonalAccount personalAccount = accountService.updatePersonalAccountLocalPermissionsById(accountId, newLocalPermissions).orElseThrow(this::accountNotFound);
        return personalAccount.getLocalPermissions().stream()
                .filter(localPermissions -> localPermissions.getLabelId().equals(labelId))
                .findFirst().map(personalAccountMapper::convertToRestLocalPermissions).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Override
    public ResponseEntity<RestKeyPair> getKeyPair() {
        Account account = accountSecurityContext
                .getAuthenticatedAccount().orElseThrow(this::accountNotFound);
        KeyPair keyPair = Optional.ofNullable(account.getActiveKeyPair()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "no active keypair found for account: " + account.getName()));
        return new ResponseEntity<>(keyPairMapper.convertToRestKeyPair(keyPair), HttpStatus.OK);
    }

    private void verifyParentLabelExists(String labelId) {
        if (!labelRepository.exists(labelId)) {
            throw labelNotFound(labelId);
        }
    }

    private ResponseStatusException labelNotFound(String labelId) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "label not found : " + labelId);
    }

    private void validateKeyId(KeyPair keyPair) {
        if (!keyPair.getKeyId().equals(KeyIdProvider.computeKeyId(keyPair.getPublicKey()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid key id : " + keyPair.getKeyId());
        }
    }

    private ResponseStatusException accountNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "personal account not found");
    }
}
