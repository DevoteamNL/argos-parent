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
import com.argosnotary.argos.service.domain.account.FinishedSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Date;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class FinishedSessionRepositoryImpl implements FinishedSessionRepository {

    public static final String COLLECTION = "finishedsession";
    public static final String SESSION_ID_FIELD = "sessionId";
    public static final String EXPIRATION_DATE_FIELD = "expirationDate";

    private final MongoTemplate template;

    @Override
    public void save(ArgosSession session) {
        template.save(session, COLLECTION);
    }

    @Override
    public boolean isUsedSessionId(String sessionId) {
        return template.exists(new Query(where(SESSION_ID_FIELD).is(sessionId)), COLLECTION);
    }

    @Override
    public void deleteExpiredSessions(Date from) {
        template.remove(new Query(where(EXPIRATION_DATE_FIELD).lt(from)), COLLECTION);
    }

}
