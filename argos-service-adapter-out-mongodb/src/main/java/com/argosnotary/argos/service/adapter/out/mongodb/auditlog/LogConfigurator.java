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
