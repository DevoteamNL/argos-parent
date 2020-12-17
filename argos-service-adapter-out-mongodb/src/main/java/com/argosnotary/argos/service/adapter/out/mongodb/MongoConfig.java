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
package com.argosnotary.argos.service.adapter.out.mongodb;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.github.cloudyrock.spring.v5.MongockSpring5.MongockApplicationRunner;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.argosnotary.argos.service.adapter.out.mongodb.release.DateToOffsetTimeConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.DocumentToReleaseDossierMetaDataConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.OffsetTimeToDateConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseDossierMetaDataToDocumentConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    public static final String CHANGELOG_SCAN_PACKAGE = "com.argosnotary.argos.service.adapter.out.mongodb.changelogs";
    
    private final List<Converter<?, ?>> converterList = new ArrayList<>();
    
    @Bean
    @Override
    public MongoCustomConversions customConversions() {
        converterList.add(new DateToOffsetTimeConverter());
        converterList.add(new OffsetTimeToDateConverter());
        converterList.add(new ReleaseDossierMetaDataToDocumentConverter());
        converterList.add(new DocumentToReleaseDossierMetaDataConverter());
        return new MongoCustomConversions(converterList);
    }

    @Value("${spring.data.mongodb.uri}")
    private String mongoURI;
    
    @Value("${spring.data.mongodb.database}")
    private String mongoDatabaseName;

    @Bean
    public MongockApplicationRunner mongogock(MongoTemplate mongoTemplate, ApplicationContext springContext) {
        return MongockSpring5.builder()
            .setDriver(SpringDataMongo3Driver.withDefaultLock(mongoTemplate))
            .addChangeLogsScanPackage(CHANGELOG_SCAN_PACKAGE)
            .setSpringContext(springContext)
            .buildApplicationRunner();
    }

    @Bean
    public GridFsTemplate gridFsTemplate() {
        return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter(mongoDbFactory(),
                customConversions(), null));
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
    
    @Override
    public MongoClient mongoClient() {
        final MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoURI))
            .build();
        return MongoClients.create(mongoClientSettings);
    }

    @Override
    protected String getDatabaseName() {
        return mongoDatabaseName;
    }
    
    @Override
    public Collection<String> getMappingBasePackages() {
        return Collections.singleton("com.argosnotary.argos.service.domain");
    }
}
