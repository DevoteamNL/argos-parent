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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ArgosSession;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestKeyPair;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestProfile;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPublicKey;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestToken;
import com.argosnotary.argos.service.domain.account.AccountSearchParams;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.account.FinishedSessionRepository;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.security.AccountSecurityContextImpl;
import com.argosnotary.argos.service.domain.security.TokenInfo;
import com.argosnotary.argos.service.domain.security.TokenProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRestServiceTest {

    private static final String NAME = "name";
    private static final String PERSONAL_ACCOUNT_NOT_FOUND = "404 NOT_FOUND \"personal account not found\"";
    public static final String ACTIVE_KEYPAIR_NOT_FOUND = "404 NOT_FOUND \"no active keypair found for account: name\"";
    private static final String ACCOUNT_ID = "accountId";
    private static final String ROLE_NAME = "roleName";
    private static final String LABEL_ID = "labelId";
    private static final String ROLE_ID = "roleId";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final char[] PRIVAT_KEY_PASSPHRASE = "test".toCharArray();
    private static final String SESSION_ID = "sessionId";
    private static final Date EXPIRATION_DATE = new Date();

    private PersonalAccountRestService service;
    @Mock
    private AccountSecurityContextImpl accountSecurityContext;

    @Mock
    private AccountKeyPairMapper keyPairMapper;
    @Mock
    private RestKeyPair restKeyPair;

    private KeyPair keyPair;

    @Mock
    private PersonalAccount personalAccount;

    @Mock
    private AccountService accountService;

    @Mock
    private PersonalAccountMapper personalAccountMapper;

    @Mock
    private RestPersonalAccount restPersonalAccount;

    @Mock
    private RestProfile restProfile;

    @Captor
    private ArgumentCaptor<AccountSearchParams> searchParamsArgumentCaptor;

    @Mock
    private LocalPermissions localPermissions;

    @Mock
    private RestLocalPermissions restLocalPermissions;

    @Captor
    private ArgumentCaptor<LocalPermissions> localPermissionsArgumentCaptor;

    @Mock
    private LabelRepository labelRepository;

    @Mock
    private RestPublicKey restPublicKey;

    @Mock
    private FinishedSessionRepository finishedSessionRepository;

    @Captor
    private ArgumentCaptor<ArgosSession> argosSessionArgumentCaptor;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private TokenInfo tokenInfo;

    @BeforeEach
    void setUp() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
        personalAccount.setAccountId(ACCOUNT_ID);
        keyPair = KeyPair.createKeyPair(PRIVAT_KEY_PASSPHRASE);

        service = new PersonalAccountRestService(accountSecurityContext, keyPairMapper, accountService, personalAccountMapper, labelRepository, finishedSessionRepository, tokenProvider);
    }

    @Test
    void getCurrentUserNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service
                .getPersonalAccountOfAuthenticatedUser());
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getPersonalAccountOfAuthenticatedUser() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToRestProfile(personalAccount)).thenReturn(restProfile);
        ResponseEntity<RestProfile> responseEntity = service.getPersonalAccountOfAuthenticatedUser();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        RestProfile restPersonalAccount = responseEntity.getBody();
        assertThat(restPersonalAccount, sameInstance(restPersonalAccount));
    }

    @Test
    void storeKeyShouldReturnSuccess() {
        when(personalAccount.getAccountId()).thenReturn(ACCOUNT_ID);
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        assertThat(service.createKey(restKeyPair).getStatusCodeValue(), is(204));
        verify(accountService).activateNewKey(ACCOUNT_ID, keyPair);
    }

    @Test
    void storeKeyShouldReturnBadRequest() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        keyPair.setKeyId("incorrect key");
        when(keyPairMapper.convertFromRestKeyPair(restKeyPair)).thenReturn(keyPair);
        assertThrows(ResponseStatusException.class, () -> service.createKey(restKeyPair));
    }

    @Test
    void storeKeyShouldReturnNotFound() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.createKey(restKeyPair));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getKeyPairShouldReturnOK() {
        when(personalAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        when(keyPairMapper.convertToRestKeyPair(keyPair)).thenReturn(restKeyPair);
        personalAccount.setActiveKeyPair(keyPair);
        ResponseEntity<RestKeyPair> responseEntity = service.getKeyPair();
        assertThat(responseEntity.getStatusCodeValue(), Matchers.is(200));
        assertThat(responseEntity.getBody(), sameInstance(restKeyPair));
    }

    @Test
    void getKeyPairShouldReturnNotFound() {
        when(personalAccount.getName()).thenReturn(NAME);
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(personalAccount));
        personalAccount.setActiveKeyPair(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getKeyPair());
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(ACTIVE_KEYPAIR_NOT_FOUND));
    }

    @Test
    void getPersonalAccountById() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccount(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<RestPersonalAccount> response = service.getPersonalAccountById(ACCOUNT_ID);
        assertThat(response.getBody(), sameInstance(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
    }

    @Test
    void getPersonalAccountByIdNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountById(ACCOUNT_ID));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void searchPersonalAccounts() {
        when(personalAccountMapper.convertToRoleId(ROLE_NAME)).thenReturn(ROLE_ID);
        when(accountService.searchPersonalAccounts(any(AccountSearchParams.class))).thenReturn(List.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccountWithoutRoles(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<List<RestPersonalAccount>> response = service.searchPersonalAccounts(ROLE_NAME, LABEL_ID, NAME, List.of(KEY1), List.of(KEY2));
        assertThat(response.getBody(), contains(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
        verify(accountService).searchPersonalAccounts(searchParamsArgumentCaptor.capture());
        AccountSearchParams searchParams = searchParamsArgumentCaptor.getValue();
        assertThat(searchParams.getLocalPermissionsLabelId(), is(Optional.of(LABEL_ID)));
        assertThat(searchParams.getRoleId(), is(Optional.of(ROLE_ID)));
        assertThat(searchParams.getName(), is(Optional.of(NAME)));
        assertThat(searchParams.getActiveKeyIds().get(), contains(KEY1));
        assertThat(searchParams.getInActiveKeyIds().get(), contains(KEY2));
    }
    


    @Test
    void searchPersonalAccountsWithRoles() {
        when(personalAccountMapper.convertToRoleId(ROLE_NAME)).thenReturn(ROLE_ID);
        when(accountService.searchPersonalAccounts(any(AccountSearchParams.class))).thenReturn(List.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccount(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<List<RestPersonalAccount>> response = service.searchPersonalAccountsWithRoles(ROLE_NAME, LABEL_ID, NAME, List.of(KEY1), List.of(KEY2));
        assertThat(response.getBody(), contains(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
        verify(accountService).searchPersonalAccounts(searchParamsArgumentCaptor.capture());
        AccountSearchParams searchParams = searchParamsArgumentCaptor.getValue();
        assertThat(searchParams.getLocalPermissionsLabelId(), is(Optional.of(LABEL_ID)));
        assertThat(searchParams.getRoleId(), is(Optional.of(ROLE_ID)));
        assertThat(searchParams.getName(), is(Optional.of(NAME)));
        assertThat(searchParams.getActiveKeyIds().get(), contains(KEY1));
        assertThat(searchParams.getInActiveKeyIds().get(), contains(KEY2));
    }

    @Test
    void updatePersonalAccountRolesById() {
        when(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME))).thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToRestPersonalAccount(personalAccount)).thenReturn(restPersonalAccount);
        ResponseEntity<RestPersonalAccount> response = service.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME));
        assertThat(response.getBody(), sameInstance(restPersonalAccount));
        assertThat(response.getStatusCodeValue(), Matchers.is(200));
    }

    @Test
    void updatePersonalAccountRolesByIdNotFound() {
        when(accountService.updatePersonalAccountRolesById(ACCOUNT_ID, List.of(ROLE_NAME))).thenReturn(Optional.empty());
        List<String> roles = List.of(ROLE_NAME);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updatePersonalAccountRolesById(ACCOUNT_ID, roles));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getAllLocalPermissions() {
        when(personalAccountMapper.convertToRestLocalPermissions(List.of(localPermissions))).thenReturn(List.of(restLocalPermissions));
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        ResponseEntity<List<RestLocalPermissions>> response = service.getAllLocalPermissions(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), contains(restLocalPermissions));
    }

    @Test
    void getLocalPermissionsForLabel() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(localPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        when(personalAccountMapper.convertToRestLocalPermission(localPermissions)).thenReturn(restLocalPermissions);
        ResponseEntity<RestLocalPermissions> response = service.getLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLocalPermissions));
    }

    @Test
    void getLocalPermissionsForLabelAccountNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID));
        assertThat(exception.getStatus().value(), is(404));
        assertThat(exception.getMessage(), is(PERSONAL_ACCOUNT_NOT_FOUND));
    }

    @Test
    void getLocalPermissionsForLabelWhenLabelIdNotFound() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(localPermissions.getLabelId()).thenReturn("otherLabel");
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        ResponseEntity<RestLocalPermissions> response = service.getLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody().getLabelId(), is(LABEL_ID));
        assertThat(response.getBody().getPermissions(), empty());
    }

    @Test
    void updateLocalPermissionsForLabel() {
        when(labelRepository.exists(LABEL_ID)).thenReturn(true);
        when(accountService.updatePersonalAccountLocalPermissionsById(eq(ACCOUNT_ID), any(LocalPermissions.class)))
                .thenReturn(Optional.of(personalAccount));
        when(personalAccountMapper.convertToLocalPermissions(List.of(RestPermission.READ))).thenReturn(List.of(Permission.READ));
        when(localPermissions.getLabelId()).thenReturn(LABEL_ID);
        when(personalAccount.getLocalPermissions()).thenReturn(List.of(localPermissions));
        when(personalAccountMapper.convertToRestLocalPermission(localPermissions)).thenReturn(restLocalPermissions);
        List<RestPermission> perms = List.of(RestPermission.READ);
        ResponseEntity<RestLocalPermissions> response = service.updateLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID, perms);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), sameInstance(restLocalPermissions));
        verify(accountService).updatePersonalAccountLocalPermissionsById(eq(ACCOUNT_ID), localPermissionsArgumentCaptor.capture());
        LocalPermissions localPermissions = localPermissionsArgumentCaptor.getValue();
        assertThat(localPermissions.getPermissions(), contains(Permission.READ));
        assertThat(localPermissions.getLabelId(), is(LABEL_ID));
    }

    @Test
    void updateLocalPermissionsForLabelNotExists() {
        when(labelRepository.exists(LABEL_ID)).thenReturn(false);
        List<RestPermission> perms = List.of(RestPermission.READ);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.updateLocalPermissionsForLabel(ACCOUNT_ID, LABEL_ID, perms));
        assertThat(exception.getStatus().value(), is(400));
        assertThat(exception.getMessage(), is("400 BAD_REQUEST \"label not found : labelId\""));
    }

    @Test
    void getPersonalAccountKeyById() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(personalAccount.getActiveKeyPair()).thenReturn(keyPair);
        when(keyPairMapper.convertToRestPublicKey(keyPair)).thenReturn(restPublicKey);
        ResponseEntity<RestPublicKey> response = service.getPersonalAccountKeyById(ACCOUNT_ID);
        assertThat(response.getStatusCodeValue(), is(200));
        assertThat(response.getBody(), is(restPublicKey));
    }

    @Test
    void getPersonalAccountKeyByIdNoAccount() {
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"personal account not found\""));
    }

    @Test
    void getPersonalAccountKeyByIdNoActiveKey() {
        when(personalAccount.getName()).thenReturn("name");
        when(accountService.getPersonalAccountById(ACCOUNT_ID)).thenReturn(Optional.of(personalAccount));
        when(personalAccount.getActiveKeyPair()).thenReturn(null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getPersonalAccountKeyById(ACCOUNT_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"no active keypair found for account: name\""));
    }

    @Test
    void logout() {
        when(tokenInfo.getSessionId()).thenReturn(SESSION_ID);
        when(tokenInfo.getExpiration()).thenReturn(EXPIRATION_DATE);
        when(accountSecurityContext.getTokenInfo()).thenReturn(Optional.of(tokenInfo));
        assertThat(service.logout().getStatusCodeValue(), is(204));
        verify(finishedSessionRepository).save(argosSessionArgumentCaptor.capture());
        ArgosSession value = argosSessionArgumentCaptor.getValue();
        assertThat(value.getSessionId(), is(SESSION_ID));
        assertThat(value.getExpirationDate(), is(EXPIRATION_DATE));
    }

    @Test
    void refreshToken() {
        when(accountSecurityContext.getTokenInfo()).thenReturn(Optional.of(tokenInfo));
        when(tokenProvider.refreshToken(tokenInfo)).thenReturn(Optional.of("token"));
        ResponseEntity<RestToken> restTokenResponse = service.refreshToken();
        assertThat(restTokenResponse.getStatusCodeValue(), is(200));
        assertThat(restTokenResponse.getBody().getToken(), is("token"));
    }

    @Test
    void refreshTokenExpired() {
        when(accountSecurityContext.getTokenInfo()).thenReturn(Optional.of(tokenInfo));
        when(tokenProvider.refreshToken(tokenInfo)).thenReturn(Optional.empty());
        ArgosError exception = assertThrows(ArgosError.class, () -> service.refreshToken());
        assertThat(exception.getMessage(), is("expired"));
        assertThat(exception.getLevel(), is(ArgosError.Level.WARNING));
    }

    @Test
    void refreshTokenNoTokenInfo() {
        when(accountSecurityContext.getTokenInfo()).thenReturn(Optional.empty());
        ArgosError exception = assertThrows(ArgosError.class, () -> service.refreshToken());
        assertThat(exception.getMessage(), is("no token info"));
        assertThat(exception.getLevel(), is(ArgosError.Level.ERROR));
    }
}