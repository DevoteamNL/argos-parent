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
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountInfo;
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
class AccountInfoMapperTest {
    private static final String ACCOUNTID = "accountId";
    private static final String NAME = "name";
    private AccountInfoMapper accountInfoMapper;

    @BeforeEach
    void setup() {
        accountInfoMapper = Mappers.getMapper(AccountInfoMapper.class);
    }

    @Test
    void convertToRestAccountInfo() {
        AccountInfo accountInfo = AccountInfo
                .builder()
                .accountId(ACCOUNTID)
                .accountType(SERVICE_ACCOUNT)
                .pathToRoot(List.of("path", "to", "root"))
                .name(NAME)
                .build();
        RestAccountInfo restAccountInfo = accountInfoMapper.convertToRestAccountInfo(accountInfo);
        assertThat(restAccountInfo.getAccountId(), is(ACCOUNTID));
        assertThat(restAccountInfo.getAccountType(), is(RestAccountType.SERVICE_ACCOUNT));
        assertThat(restAccountInfo.getName(), is(NAME));
        assertThat(restAccountInfo.getPath(), is("root/to/path"));
    }
}