/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.in.rest.account;


import com.rabobank.argos.domain.account.Account;
import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.domain.key.KeyIdProvider;
import com.rabobank.argos.domain.key.KeyIdProviderImpl;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.handler.PersonalAccountApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.security.AccountSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PersonalAccountRestService implements PersonalAccountApi {

    private final AccountSecurityContext accountSecurityContext;
    private final AccountKeyPairMapper keyPairMapper;
    private final KeyIdProvider keyIdProvider = new KeyIdProviderImpl();
    private final AccountService accountService;
    private final PersonalAccountMapper personalAccountMapper;


    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<RestPersonalAccount> getPersonalAccountOfAuthenticatedUser() {
        return accountSecurityContext.getAuthenticatedAccount()
                .map(account -> (PersonalAccount) account)
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::profileNotFound);
    }

    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<Void> createKey(@Valid RestKeyPair restKeyPair) {
        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(this::profileNotFound);
        KeyPair keyPair = keyPairMapper.convertFromRestKeyPair(restKeyPair);
        validateKeyId(keyPair);
        accountService.activateNewKey(account.getAccountId(), keyPair);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<RestPersonalAccount> getPersonalAccountById(String accountId) {
        return accountService.getPersonalAccountById(accountId)
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::profileNotFound);
    }

    @Override
    public ResponseEntity<List<RestPersonalAccount>> searchPersonalAccounts(String roleName) {
        return ResponseEntity.ok(accountService.searchPersonalAccounts(roleName).stream()
                .map(personalAccountMapper::convertToRestPersonalAccountWithoutRoles).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<RestPersonalAccount> updatePersonalAccountRolesById(String accountId, List<String> roleNames) {
        return accountService.updatePersonalAccountRolesById(accountId, roleNames)
                .map(personalAccountMapper::convertToRestPersonalAccount)
                .map(ResponseEntity::ok).orElseThrow(this::profileNotFound);
    }


    @Override
    public ResponseEntity<RestKeyPair> getKeyPair() {
        Account account = accountSecurityContext
                .getAuthenticatedAccount().orElseThrow(this::profileNotFound);
        KeyPair keyPair = Optional.ofNullable(account.getActiveKeyPair()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "no active keypair found for account: " + account.getName()));
        return new ResponseEntity<>(keyPairMapper.convertToRestKeyPair(keyPair), HttpStatus.OK);
    }

    private void validateKeyId(KeyPair keyPair) {
        if (!keyPair.getKeyId().equals(keyIdProvider.computeKeyId(keyPair.getPublicKey()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid key id : " + keyPair.getKeyId());
        }
    }

    private ResponseStatusException profileNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "personal account not found");
    }
}
