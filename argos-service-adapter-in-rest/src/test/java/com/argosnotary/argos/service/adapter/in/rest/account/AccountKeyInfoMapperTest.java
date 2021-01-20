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