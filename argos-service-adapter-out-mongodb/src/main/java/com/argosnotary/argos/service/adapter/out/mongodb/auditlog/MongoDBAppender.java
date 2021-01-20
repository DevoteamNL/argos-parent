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
package com.argosnotary.argos.service.adapter.out.mongodb.auditlog;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoDBAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private final MongoTemplate mongoTemplate;
    static final String COLLECTION = "auditlogs";
    @Override
    protected void append(ILoggingEvent eventObject) {
        Document logEntry = new Document();
        logEntry.append("timestamp", new Date(eventObject.getTimeStamp()));
        logEntry.append("message", eventObject.getFormattedMessage());
        eventObject.getMDCPropertyMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach(logEntry::append);
        mongoTemplate.insert(logEntry, COLLECTION);
    }


}
