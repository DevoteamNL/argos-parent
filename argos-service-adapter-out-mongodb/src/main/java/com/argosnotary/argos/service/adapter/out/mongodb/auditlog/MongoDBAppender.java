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
