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
        accountInfoRepository.findByKeyIds(Collections.singletonList("keyId"));
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