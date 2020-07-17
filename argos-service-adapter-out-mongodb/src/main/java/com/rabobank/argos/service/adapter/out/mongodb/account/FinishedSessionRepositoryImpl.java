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

import com.rabobank.argos.domain.account.ArgosSession;
import com.rabobank.argos.service.domain.account.FinishedSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class FinishedSessionRepositoryImpl implements FinishedSessionRepository {

    static final String COLLECTION = "finishedsession";
    static final String SESSION_ID_FIELD = "sessionId";
    static final String EXPIRATION_DATE_FIELD = "expirationDate";

    private final MongoTemplate template;

    @Override
    public void save(ArgosSession session) {
        template.save(session, COLLECTION);
    }

    @Override
    public boolean hasSessionId(String sessionId) {
        return template.exists(new Query(where(SESSION_ID_FIELD).is(sessionId)), COLLECTION);
    }

    @Override
    public void deleteExpiredSessions(Date from) {
        template.remove(new Query(where(EXPIRATION_DATE_FIELD).lt(from)), COLLECTION);
    }

}
