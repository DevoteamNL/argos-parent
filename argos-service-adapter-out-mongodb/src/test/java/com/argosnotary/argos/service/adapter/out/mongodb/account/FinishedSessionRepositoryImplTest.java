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
package com.argosnotary.argos.service.adapter.out.mongodb.account;

import com.argosnotary.argos.domain.account.ArgosSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static com.argosnotary.argos.service.adapter.out.mongodb.account.FinishedSessionRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinishedSessionRepositoryImplTest {

    private static final String SESSION_ID = "sessionId";
    @Mock
    private MongoTemplate template;

    @Mock
    private ArgosSession session;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    private FinishedSessionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new FinishedSessionRepositoryImpl(template);
    }

    @Test
    void save() {
        repository.save(session);
        verify(template).save(session, COLLECTION);
    }

    @Test
    void hadSessionId() {
        when(template.exists(any(Query.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.hasSessionId(SESSION_ID), is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"sessionId\" : \"sessionId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void deleteExpiredSessions() {
        Date date = Date.from(ZonedDateTime.of(2020, 1, 15, 14, 12, 1, 0, ZoneId.of("Z")).toInstant());
        repository.deleteExpiredSessions(date);
        verify(template).remove(queryArgumentCaptor.capture(), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"expirationDate\" : { \"$lt\" : { \"$date\" : \"2020-01-15T14:12:01Z\"}}}, Fields: {}, Sort: {}"));
    }
}