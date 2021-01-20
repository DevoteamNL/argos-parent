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
package com.argosnotary.argos.service.adapter.out.mongodb.account;

import com.argosnotary.argos.domain.account.AccountInfo;
import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.domain.account.AccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collections;

import static com.argosnotary.argos.service.adapter.out.mongodb.account.AccountInfoRepositoryImpl.ACCOUNTS_INFO_VIEW;
import static com.argosnotary.argos.service.adapter.out.mongodb.account.AccountInfoRepositoryImpl.ACCOUNTS_KEYINFO_VIEW;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountInfoRepositoryImplTest {
    @Mock
    private MongoTemplate template;
    @Mock
    private AccountKeyInfo accountKeyInfo;

    @Mock
    private AccountInfo accountInfo;

    private AccountInfoRepositoryImpl accountInfoRepository;
    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @BeforeEach
    void setup() {
        accountInfoRepository = new AccountInfoRepositoryImpl(template);
    }

    @Test
    void findByKeyIds() {
        when(template.find(any(), eq(AccountKeyInfo.class), eq(ACCOUNTS_KEYINFO_VIEW))).thenReturn(Collections.singletonList(accountKeyInfo));
        accountInfoRepository.findByKeyIds(Collections.singleton("keyId"));
        verify(template).find(queryArgumentCaptor.capture(), eq(AccountKeyInfo.class), eq(ACCOUNTS_KEYINFO_VIEW));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"key.keyId\" : { \"$in\" : [\"keyId\"]}}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByNameIdPathToRootAndAccountType() {
        when(template.find(any(), eq(AccountInfo.class), eq(ACCOUNTS_INFO_VIEW))).thenReturn(Collections.singletonList(accountInfo));
        accountInfoRepository.findByNameIdPathToRootAndAccountType("name", Collections.singletonList("id"), AccountType.SERVICE_ACCOUNT);
        verify(template).find(queryArgumentCaptor.capture(), eq(AccountInfo.class), eq(ACCOUNTS_INFO_VIEW));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"name\" : { \"$regularExpression\" : { \"pattern\" : \".*name.*\", \"options\" : \"i\"}}, \"$or\" : [{ \"parentLabelId\" : null}, { \"parentLabelId\" : { \"$in\" : [\"id\"]}}], \"accountType\" : \"SERVICE_ACCOUNT\"}, Fields: {}, Sort: {}"));
    }
}