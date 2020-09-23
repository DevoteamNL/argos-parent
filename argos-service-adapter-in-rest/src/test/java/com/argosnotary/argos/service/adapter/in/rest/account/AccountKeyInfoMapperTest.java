/*
 * Copyright (C) 2020 Argos Notary Cooperative
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

import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.domain.account.KeyInfo;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountKeyInfo;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.argosnotary.argos.domain.account.AccountType.SERVICE_ACCOUNT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class AccountKeyInfoMapperTest {
    protected static final String ACCOUNTID = "accountid";
    protected static final String KEY_ID = "keyId";
    protected static final String NAME = "name";
    private AccountKeyInfoMapper accountKeyInfoMapper;

    @BeforeEach
    void setup() {
        accountKeyInfoMapper = Mappers.getMapper(AccountKeyInfoMapper.class);
    }

    @Test
    void convertToRestAccountInfo() {
        AccountKeyInfo accountKeyInfo = AccountKeyInfo
                .builder()
                .accountId(ACCOUNTID)
                .accountType(SERVICE_ACCOUNT)
                .key(KeyInfo.builder().keyId(KEY_ID).status(KeyInfo.KeyStatus.ACTIVE).build())
                .pathToRoot(List.of("path", "to", "root"))
                .name(NAME)
                .build();
        RestAccountKeyInfo restAccountKeyInfo = accountKeyInfoMapper.convertToRestAccountKeyInfo(accountKeyInfo);
        assertThat(restAccountKeyInfo.getAccountId(), is(ACCOUNTID));
        assertThat(restAccountKeyInfo.getName(), is(NAME));
        assertThat(restAccountKeyInfo.getAccountType(), is(RestAccountType.SERVICE_ACCOUNT));
        assertThat(restAccountKeyInfo.getKeyId(), is(KEY_ID));
        assertThat(restAccountKeyInfo.getKeyStatus(), is(RestAccountKeyInfo.KeyStatusEnum.ACTIVE));
        assertThat(restAccountKeyInfo.getPath(), is("root/to/path"));
    }
}