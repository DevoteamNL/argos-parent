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
package com.argosnotary.argos.service.domain.account;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.argosnotary.argos.domain.permission.Role.ADMINISTRATOR;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final String ACCOUNT_NAME = "accountName";
    private static final String ACCOUNT_ID = "accountId";
    private static final String EMAIL = "email";
    private static final Role ROLE = Role.ADMINISTRATOR;
    private static final String KEY_ID = "keyId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final String LABEL_ID = "labelId";
    private static final String OTHER_LABEL_ID = "other";
    private static final String ADMIN_ACCOUNT_ID = "adminAccountId";


    private KeyPair activeKeyPair;
    private KeyPair inactiveKeyPair;
    private Set<KeyPair> inactiveKeyPairSet;
    private KeyPair newKeyPair;

    private AccountServiceImpl accountService;

    @Mock
    private ServiceAccountRepository serviceAccountRepository;

    @Mock
    private PersonalAccountRepository personalAccountRepository;

    private PersonalAccount account;

    @Mock
    private ServiceAccountKeyPair serviceAccountKeyPair;

    private PersonalAccount existingAccount;

    private ServiceAccount existingServiceAccount;

    private ServiceAccount serviceAccount;

    @Mock
    private AccountSearchParams params;

    private LocalPermissions newLocalPermissions;

    private LocalPermissions existingLocalPermissions;

    @Captor
    private ArgumentCaptor<Set<LocalPermissions>> localPermissionsSetArgumentCaptor;

    private Role adminRole = Role.ADMINISTRATOR;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    private Account adminAccount;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, PemGenerationException {
        activeKeyPair = KeyPair.createKeyPair("test".toCharArray());
        inactiveKeyPair = KeyPair.createKeyPair("test".toCharArray());
        newKeyPair = KeyPair.createKeyPair("test".toCharArray());
        accountService = new AccountServiceImpl(serviceAccountRepository, personalAccountRepository, accountSecurityContext);
        account = new PersonalAccount("accountName", null, null, null, null, null, null, null);
        adminAccount = new PersonalAccount("adminAccount", null, activeKeyPair, null, null, null, Set.of(ADMINISTRATOR), null);
        existingAccount = new PersonalAccount("existingAccount", null, null, null, null, null, null, null);
        serviceAccount = new ServiceAccount(null, null, null, null);
        existingServiceAccount = new ServiceAccount(null, null, null, null);
        existingLocalPermissions = LocalPermissions.builder().labelId(LABEL_ID).permissions(Set.of(Permission.READ)).build();
        newLocalPermissions = LocalPermissions.builder().labelId(OTHER_LABEL_ID).permissions(Set.of(Permission.TREE_EDIT)).build();
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndNoInactiveKeys() {
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, newKeyPair), is(Optional.of(account)));
        assertThat(account.getInactiveKeyPairs(), empty());
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKey() {
        account.setActiveKeyPair(activeKeyPair);
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, newKeyPair), is(Optional.of(account)));
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndemptySet() {
        account.setActiveKeyPair(activeKeyPair);
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, newKeyPair), is(Optional.of(account)));
        assertThat(account.getInactiveKeyPairs(), contains(activeKeyPair));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
    }

    @Test
    void deactivateKeyPairNoActiveKeyAndInactiveKeyPair() {
        account.setActiveKeyPair(activeKeyPair);
        account.getInactiveKeyPairs().add(inactiveKeyPair);
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        accountService.activateNewKey(ACCOUNT_ID, newKeyPair);
        assertThat(account.getInactiveKeyPairs(), is(Set.of(inactiveKeyPair, activeKeyPair)));
        assertThat(account.getActiveKeyPair(), sameInstance(newKeyPair));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void authenticateFirstUserShouldBeAssignedRoleAdminAndRoleUser() {
        account.setEmail(EMAIL);
        when(personalAccountRepository.getTotalNumberOfAccounts()).thenReturn(0L);
        PersonalAccount personalAccount = accountService.authenticateUser(account).get();
        assertThat(personalAccount, sameInstance(account));
        assertThat(personalAccount.getRoles(), is(Set.of(ADMINISTRATOR)));
        verify(personalAccountRepository).save(personalAccount);
    }

    @Test
    void authenticateSecondUserShouldHaveNoRole() {
        account.setEmail(EMAIL);
        when(personalAccountRepository.getTotalNumberOfAccounts()).thenReturn(1L);
        when(personalAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        PersonalAccount personalAccount = accountService.authenticateUser(account).get();
        assertThat(personalAccount, sameInstance(account));
        assertThat(personalAccount.getRoles(), is(emptySet()));
        verify(personalAccountRepository).save(personalAccount);
    }

    @Test
    void authenticateUserSecondTime() {
        account.setEmail(EMAIL);
        when(personalAccountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingAccount));
        PersonalAccount personalAccount = accountService.authenticateUser(account).get();
        assertThat(personalAccount, sameInstance(existingAccount));
        verify(personalAccountRepository).update(personalAccount);
    }

    @Test
    void activateNewKey() {
        when(serviceAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(serviceAccount));
        assertThat(accountService.activateNewKey(ACCOUNT_ID, serviceAccountKeyPair), is(Optional.of(serviceAccount)));
        verify(serviceAccountRepository).update(serviceAccount);
    }

    @Test
    void keyPairExistsNot() {
        assertThat(accountService.keyPairExists(KEY_ID), is(false));
    }

    @Test
    void serviceAccountKeyPairExists() {
        when(serviceAccountRepository.activeKeyExists(KEY_ID)).thenReturn(true);
        assertThat(accountService.keyPairExists(KEY_ID), is(true));
    }

    @Test
    void personalAccountKeyPairExists() {
        when(personalAccountRepository.activeKeyExists(KEY_ID)).thenReturn(true);
        assertThat(accountService.keyPairExists(KEY_ID), is(true));
    }

    @Test
    void findKeyPairByKeyIdNot() {
        assertThat(accountService.findKeyPairByKeyId(KEY_ID), is(Optional.empty()));
    }

    @Test
    void serviceAccountFindKeyPairByKeyId() {
        serviceAccount.setActiveKeyPair(activeKeyPair);
        when(serviceAccountRepository.findByActiveKeyId(KEY_ID)).thenReturn(Optional.of(serviceAccount));
        assertThat(accountService.findKeyPairByKeyId(KEY_ID), is(Optional.of(activeKeyPair)));
    }

    @Test
    void personalAccountFindKeyPairByKeyId() {
        account.setActiveKeyPair(activeKeyPair);
        when(personalAccountRepository.findByActiveKeyId(KEY_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.findKeyPairByKeyId(KEY_ID), is(Optional.of(activeKeyPair)));
    }

    @Test
    void getPersonalAccountById() {
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.getPersonalAccountById(ACCOUNT_ID), is(Optional.of(account)));
    }

    @Test
    void searchPersonalAccounts() {
        when(personalAccountRepository.search(params)).thenReturn(List.of(account));
        assertThat(accountService.searchPersonalAccounts(params), contains(account));
    }


    @Test
    void updatePersonalAccountRolesById() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(adminAccount));

        account.setAccountId(ACCOUNT_ID);
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, Set.of(ROLE)), is(Optional.of(account)));
        assertThat(account.getRoles(), is(Set.of(ROLE)));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void administratorUpdatePersonalAccountRolesById() {
        account.setAccountId(ACCOUNT_ID);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, Set.of(ADMINISTRATOR)), is(Optional.of(account)));
        assertThat(account.getRoles(), is(Set.of(ADMINISTRATOR)));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountRolesByIdNotAllowed() {
        account.setAccountId(ACCOUNT_ID);
        account.setRoles(Set.of(ADMINISTRATOR));
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        ArgosError exception = assertThrows(ArgosError.class, () -> accountService.updatePersonalAccountRolesById(ACCOUNT_ID, emptySet()));
        assertThat(exception.getMessage(), is("administrators can not unassign there own administrator role"));
        assertThat(exception.getLevel(), is(ArgosError.Level.WARNING));
    }

    @Test
    void updatePersonalAccountAdministratorNoAuthenticatedAccount() {

        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        Set<Role> roles = Set.of(ROLE);
        ArgosError exception = assertThrows(ArgosError.class, () -> accountService.updatePersonalAccountRolesById(ACCOUNT_ID, roles));
        assertThat(exception.getMessage(), is("no authenticated account"));
        assertThat(exception.getLevel(), is(ArgosError.Level.ERROR));
    }

    @Test
    void save() {
        accountService.save(serviceAccount);
        verify(serviceAccountRepository).save(serviceAccount);
    }

    @Test
    void findServiceAccountById() {
        when(serviceAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(serviceAccount));
        assertThat(accountService.findServiceAccountById(ACCOUNT_ID), is(Optional.of(serviceAccount)));
    }

    @Test
    void update() {
        serviceAccount = ServiceAccount.builder().name(ACCOUNT_NAME).parentLabelId(PARENT_LABEL_ID).build();
        when(serviceAccountRepository.findById(ACCOUNT_ID)).thenReturn(Optional.of(serviceAccount));
        assertThat(accountService.update(ACCOUNT_ID, serviceAccount), is(Optional.of(serviceAccount)));
        verify(serviceAccountRepository).update(serviceAccount);
    }
    
    @Test
    void updatePersonalAccountLocalPermissionsByIdExistingLocalPermissionsNoChange() {
        account.setLocalPermissions(Set.of(existingLocalPermissions));
        newLocalPermissions = LocalPermissions.builder().labelId(LABEL_ID).permissions(Set.of(Permission.READ)).build();
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.of(account)));
        assertThat(account.getLocalPermissions(), is(Set.of(existingLocalPermissions)));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdExistingLocalPermissionsDelete() {
        account.setLocalPermissions(Set.of(existingLocalPermissions));
        newLocalPermissions.setLabelId(LABEL_ID);
        newLocalPermissions.setPermissions(emptySet());
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.of(account)));
        assertThat(account.getLocalPermissions(), is(emptySet()));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdNonExistingLocalPermissions() {
        account.setLocalPermissions(Set.of(existingLocalPermissions));
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(account));
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.of(account)));
        assertThat(account.getLocalPermissions(), is(Set.of(existingLocalPermissions, newLocalPermissions)));
        verify(personalAccountRepository).update(account);
    }

    @Test
    void updatePersonalAccountLocalPermissionsByIdAccountNotFound() {
        when(personalAccountRepository.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());
        assertThat(accountService.updatePersonalAccountLocalPermissionsById(ACCOUNT_ID, newLocalPermissions), is(Optional.empty()));
        verifyNoMoreInteractions(personalAccountRepository);
    }

    @Test
    void deleteServiceAccount() {
        accountService.deleteServiceAccount(ACCOUNT_ID);
        verify(serviceAccountRepository).delete(ACCOUNT_ID);
    }
}