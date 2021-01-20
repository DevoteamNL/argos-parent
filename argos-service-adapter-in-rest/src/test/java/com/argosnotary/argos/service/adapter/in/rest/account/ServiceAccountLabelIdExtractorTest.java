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

import com.argosnotary.argos.service.domain.account.ServiceAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceAccountLabelIdExtractorTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String PARENT_LABEL_ID = "parentLabelId";

    @Mock
    private ServiceAccountRepository serviceAccountRepository;
    private ServiceAccountLabelIdExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ServiceAccountLabelIdExtractor(serviceAccountRepository);
    }

    @Test
    void extractLabelId() {
        when(serviceAccountRepository.findParentLabelIdByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(PARENT_LABEL_ID));
        assertThat(extractor.extractLabelId(null, ACCOUNT_ID), is(Optional.of(PARENT_LABEL_ID)));
    }
}