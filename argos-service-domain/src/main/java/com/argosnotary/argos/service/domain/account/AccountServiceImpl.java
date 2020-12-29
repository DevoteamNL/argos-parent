/*
 * Copyright (C) 2020 Argos Notary
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
package com.argosnotary.argos.service.domain.account;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.argosnotary.argos.domain.permission.Role.ADMINISTRATOR;


@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final ServiceAccountRepository serviceAccountRepository;
    private final PersonalAccountRepository personalAccountRepository;
    private final AccountSecurityContext accountSecurityContext;

    @Override
    public Optional<PersonalAccount> activateNewKey(String accountId, KeyPair newKeyPair) {
        return personalAccountRepository.findByAccountId(accountId).map(account -> {
            activateNewKey(account, newKeyPair);
            personalAccountRepository.update(account);
            return account;
        });
    }

    @Override
    public Optional<ServiceAccount> activateNewKey(String accountId, ServiceAccountKeyPair newKeyPair) {
        return serviceAccountRepository.findById(accountId).map(account -> {
            activateNewKey(account, newKeyPair);
            serviceAccountRepository.update(account);
            return account;
        });
    }

    @Override
    public boolean keyPairExists(String keyId) {
        return serviceAccountRepository.activeKeyExists(keyId) ||
                personalAccountRepository.activeKeyExists(keyId);
    }

    @Override
    public Optional<KeyPair> findKeyPairByKeyId(String keyId) {
        return serviceAccountRepository
                .findByActiveKeyId(keyId).map(serviceAccount -> (Account) serviceAccount)
                .or(() -> personalAccountRepository.findByActiveKeyId(keyId)).map(Account::getActiveKeyPair);
    }

    @Override
    public Optional<PersonalAccount> authenticateUser(PersonalAccount personalAccount) {
        return Optional.of(personalAccountRepository.findByEmail(personalAccount.getEmail()).map(currentAccount -> {
            currentAccount.setName(personalAccount.getName());
            personalAccountRepository.update(currentAccount);
            return currentAccount;
        }).orElseGet(() -> {
            if (getTotalPersonalAccounts() == 0) {
                makeAdministrator(personalAccount);
            }
            personalAccountRepository.save(personalAccount);
            return personalAccount;
        }));
    }

    @Override
    public Optional<PersonalAccount> getPersonalAccountById(String accountId) {
        return personalAccountRepository.findByAccountId(accountId);
    }

    @Override
    public List<PersonalAccount> searchPersonalAccounts(AccountSearchParams params) {
        return personalAccountRepository.search(params);
    }

    @Override
    public Optional<PersonalAccount> updatePersonalAccountRolesById(String accountId, Set<Role> roles) {
        return personalAccountRepository.findByAccountId(accountId).map(personalAccount -> {
            checkAdministratorRoleChange(personalAccount, roles);
            personalAccount.setRoles(roles);
            personalAccountRepository.update(personalAccount);
            return personalAccount;
        });
    }

    private void checkAdministratorRoleChange(PersonalAccount personalAccount, Set<Role> roles) {
        Account authenticatedAccount = accountSecurityContext.getAuthenticatedAccount().orElseThrow(() -> new ArgosError("no authenticated account"));
        if (authenticatedAccount.getAccountId().equals(personalAccount.getAccountId()) && !roles.contains(ADMINISTRATOR) &&
                personalAccount.getRoles().contains(ADMINISTRATOR)) {
            throw new ArgosError("administrators can not unassign there own administrator role", ArgosError.Level.WARNING);
        }
    }

    @Override
    public Optional<PersonalAccount> updatePersonalAccountLocalPermissionsById(String accountId, LocalPermissions newLocalPermissions) {
        return personalAccountRepository.findByAccountId(accountId).map(personalAccount -> {
            if (newLocalPermissions.getPermissions().isEmpty()) {
                removeLocalPermissions(newLocalPermissions, personalAccount);
            } else {
                addOrUpdateLocalPermissions(newLocalPermissions, personalAccount);
            }
            personalAccountRepository.update(personalAccount);
            return personalAccount;
        });
    }

    private void addOrUpdateLocalPermissions(LocalPermissions newLocalPermissions, PersonalAccount personalAccount) {
        Set<LocalPermissions> localPermissionsSet = personalAccount.getLocalPermissions().stream()
                .map(localPermissions -> {
                    if (localPermissions.getLabelId().equals(newLocalPermissions.getLabelId())) {
                        localPermissions.setPermissions(
                                newLocalPermissions.getPermissions().stream().collect(Collectors.toSet()));
                    }
                    return localPermissions;
                }).collect(Collectors.toSet());
        // in case newLocalPermissions not in localpermissions
        localPermissionsSet.add(newLocalPermissions);
        personalAccount.setLocalPermissions(localPermissionsSet);
    }

    private void removeLocalPermissions(LocalPermissions newLocalPermissions, PersonalAccount personalAccount) {
        Set<LocalPermissions> localPermissions = personalAccount.getLocalPermissions().stream()
                .filter(localPermission -> !localPermission.getLabelId().equals(newLocalPermissions.getLabelId()))
                .collect(Collectors.toSet());
        personalAccount.setLocalPermissions(localPermissions);
    }

    @Override
    public void save(ServiceAccount serviceAccount) {
        serviceAccountRepository.save(serviceAccount);
    }

    @Override
    public void deleteServiceAccount(String accountId) {
        serviceAccountRepository.delete(accountId);
    }

    @Override
    public Optional<ServiceAccount> findServiceAccountById(String accountId) {
        return serviceAccountRepository.findById(accountId);
    }

    @Override
    public Optional<ServiceAccount> update(String accountId, ServiceAccount serviceAccount) {
        return serviceAccountRepository.findById(accountId).map(account -> {
            account.setParentLabelId(serviceAccount.getParentLabelId());
            account.setName(serviceAccount.getName());
            account.setEmail(serviceAccount.getEmail());
            serviceAccountRepository.update(account);
            return account;
        });
    }

    @Override
    public boolean serviceAccountExists(String serviceAccountId) {
        return serviceAccountRepository.exists(serviceAccountId);
    }

    private long getTotalPersonalAccounts() {
        return personalAccountRepository.getTotalNumberOfAccounts();
    }

    private void makeAdministrator(PersonalAccount personalAccount) {
        log.info("Assigned administrator role to personal account " + personalAccount.getName());
        personalAccount.addRole(ADMINISTRATOR);
    }

    private void activateNewKey(Account account, KeyPair newKeyPair) {
        deactivateKeyPair(account);
        account.setActiveKeyPair(newKeyPair);
    }

    private void deactivateKeyPair(Account account) {
        Optional.ofNullable(account.getActiveKeyPair()).ifPresent(keyPair -> {
            Set<KeyPair> inactiveKeyPairs = Optional.ofNullable(account.getInactiveKeyPairs()).orElse(new HashSet<>());
            inactiveKeyPairs.add(keyPair);
            account.setActiveKeyPair(null);
            account.setInactiveKeyPairs(inactiveKeyPairs);
        });
    }

}
