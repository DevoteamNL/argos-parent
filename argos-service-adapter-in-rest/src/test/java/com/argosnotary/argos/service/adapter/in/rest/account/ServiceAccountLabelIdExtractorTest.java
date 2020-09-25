/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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