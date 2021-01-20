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

import com.argosnotary.argos.domain.account.AccountInfo;
import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountInfo;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountKeyInfo;
import com.argosnotary.argos.service.domain.account.AccountInfoRepository;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchAccountRestserviceTest {
    protected static final String KEY_ID = "keyId";
    protected static final String SUPPLY_CHAIN_ID = "supplyChainId";
    @Mock
    private AccountInfoRepository accountInfoRepository;

    @Mock
    private HierarchyRepository hierarchyRepository;
    @Mock
    private AccountKeyInfoMapper accountKeyInfoMapper;

    @Mock
    private AccountKeyInfo accountKeyInfo;

    @Mock
    private RestAccountKeyInfo restAccountKeyInfo;

    @Mock
    private AccountInfoMapper accountInfoMapper;

    @Mock
    private TreeNode treeNode;

    @Mock
    private AccountInfo accountInfo;

    @Mock
    private RestAccountInfo restAccountInfo;

    private SearchAccountRestservice searchAccountRestservice;

    @BeforeEach
    void setUp() {
        searchAccountRestservice = new SearchAccountRestservice(hierarchyRepository, accountInfoRepository, accountKeyInfoMapper, accountInfoMapper);
    }

    @Test
    void searchKeysFromAccountShouldReturn200() {
        when(accountInfoRepository.findByKeyIds(any())).thenReturn(Collections.singletonList(accountKeyInfo));
        when(restAccountKeyInfo.getKeyId()).thenReturn(KEY_ID);
        when(accountKeyInfoMapper.convertToRestAccountKeyInfo(accountKeyInfo)).thenReturn(restAccountKeyInfo);
        ResponseEntity<List<RestAccountKeyInfo>> responseEntity = searchAccountRestservice.searchKeysFromAccount(SUPPLY_CHAIN_ID, Collections.singletonList(KEY_ID));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    void searchAccountsShouldReturn200() {
        when(accountInfoRepository
                .findByNameIdPathToRootAndAccountType(any(), any(), any()))
                .thenReturn(Collections.singletonList(accountInfo));
        when(accountInfoMapper.convertToRestAccountInfo(accountInfo)).thenReturn(restAccountInfo);
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0))
                .thenReturn(Optional.of(treeNode));
        when(treeNode.getIdPathToRoot()).thenReturn(Collections.singletonList("id"));
        ResponseEntity<List<RestAccountInfo>> responseEntity = searchAccountRestservice.searchAccounts(SUPPLY_CHAIN_ID, "name", null);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
    }

}