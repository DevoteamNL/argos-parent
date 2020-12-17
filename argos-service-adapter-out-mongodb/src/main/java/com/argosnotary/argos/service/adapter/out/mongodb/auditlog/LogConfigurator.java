/*
 * Copyright (C) 2020 Argos Notary
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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import static com.argosnotary.argos.service.domain.auditlog.AuditLogAdvisor.ARGOS_AUDIT_LOG;

@Component
@Slf4j
@Profile("integration-test")
public class LogConfigurator {

    @EventListener
    public void configureMongoDBLogger(ContextRefreshedEvent contextRefreshedEvent) {
        MongoTemplate mongoTemplate = contextRefreshedEvent.getApplicationContext().getBean("mongoTemplate", MongoTemplate.class);
        LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = (Logger) LoggerFactory.getLogger(ARGOS_AUDIT_LOG);
        MongoDBAppender mongoDBAppender = new MongoDBAppender(mongoTemplate);
        mongoDBAppender.setContext(logContext);
        mongoDBAppender.start();
        logger.addAppender(mongoDBAppender);
        log.info("mongoDB Log Appender added ");
    }

}
