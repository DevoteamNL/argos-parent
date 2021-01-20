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
package com.argosnotary.argos.service.security;

import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.service.domain.security.AccountUserDetailsAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogContextHelperTest {
    private static final String ACCOUNT_ID = "accountId";
    private static final String ACCOUNT_NAME = "accountName";
    @Mock
    private AccountUserDetailsAdapter accountUserDetailsAdapter;
    @Mock
    private Account account;


    private LogContextHelper logContextHelper;

    @BeforeEach
    void setup() {
        logContextHelper = new LogContextHelper();


    }

    @Test
    void addAccountInfoToLogContext() {
        when(accountUserDetailsAdapter.getAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn(ACCOUNT_ID);
        when(account.getName()).thenReturn(ACCOUNT_NAME);
        logContextHelper.addAccountInfoToLogContext(accountUserDetailsAdapter);
        verify(account, times(1)).getName();
        verify(account, times(1)).getAccountId();
        assertThat(MDC.get(ACCOUNT_ID), is(ACCOUNT_ID));
        assertThat(MDC.get(ACCOUNT_NAME), is(ACCOUNT_NAME));
    }

    @Test
    void addTraceIdToLogContext() {
        logContextHelper.addTraceIdToLogContext();
        assertThat(MDC.get("traceId"), matchesPattern("[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}"));
    }

    @AfterEach
    void removeFromMDC() {
        MDC.clear();
    }
}