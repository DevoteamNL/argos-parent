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
package com.rabobank.argos.service.adapter.out.mongodb.account;

import com.rabobank.argos.domain.account.AccountKeyInfo;
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

import static com.rabobank.argos.service.adapter.out.mongodb.account.AccountInfoRepositoryImpl.ACCOUNTS_KEYINFO_VIEW;
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
    private AccountInfoRepositoryImpl accountInfoRepository;
    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @BeforeEach
    void setup() {
        accountInfoRepository = new AccountInfoRepositoryImpl(template);

    }

    @Test
    public void findByKeyIds() {
        when(template.find(any(), eq(AccountKeyInfo.class), eq(ACCOUNTS_KEYINFO_VIEW))).thenReturn(Collections.singletonList(accountKeyInfo));
        accountInfoRepository.findByKeyIds(Collections.singletonList("keyId"));
        verify(template).find(queryArgumentCaptor.capture(), eq(AccountKeyInfo.class), eq(ACCOUNTS_KEYINFO_VIEW));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"key.keyId\" : { \"$in\" : [\"keyId\"]}}, Fields: {}, Sort: {}"));
    }
}