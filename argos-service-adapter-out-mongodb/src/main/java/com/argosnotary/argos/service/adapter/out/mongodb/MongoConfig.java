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
