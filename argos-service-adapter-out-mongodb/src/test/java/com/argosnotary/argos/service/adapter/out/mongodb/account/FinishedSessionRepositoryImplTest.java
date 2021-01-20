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
        assertThat(repository.isUsedSessionId(SESSION_ID), is(true));
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