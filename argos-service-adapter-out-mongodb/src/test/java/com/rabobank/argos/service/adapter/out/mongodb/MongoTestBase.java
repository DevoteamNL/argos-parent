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
package com.rabobank.argos.service.adapter.out.mongodb;

import static de.flapdoodle.embed.process.config.io.ProcessOutput.getDefaultInstanceSilent;

import java.io.IOException;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
import com.mongodb.client.MongoClients;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.runtime.Network;

public class MongoTestBase {
    
    private MongodExecutable mongodExecutable;
    
    private int port;
    
    public MongoTemplate startDb(String scanPackage) throws IOException {
        String ip = "localhost";
        port = Network.getFreeServerPort();
        IMongodConfig mongodConfig = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                .net(new Net(ip, port, Network.localhostIsIPv6()))
                .build();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder().defaults(Command.MongoD).processOutput(getDefaultInstanceSilent()).build();
        MongodStarter starter = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = starter.prepare(mongodConfig);
        mongodExecutable.start();
        MongoTemplate mongoTemplate = new MongoTemplate(MongoClients.create(getConnectionString()), "test");
        SpringDataMongo3Driver driver = SpringDataMongo3Driver.withDefaultLock(mongoTemplate);
        MongockSpring5.builder()
            .setDriver(driver)
            .addChangeLogsScanPackage(scanPackage)
            .setSpringContext(getApplicationContext()).buildApplicationRunner().execute();
        return mongoTemplate;
    }
    
    protected ApplicationContext getApplicationContext() {
        ApplicationContext context = Mockito.mock(ApplicationContext.class);
        Mockito.when(context.getBean(Environment.class)).thenReturn(Mockito.mock(Environment.class));
        return context;
    }
    
    public void stopDb() {
        mongodExecutable.stop();
    }
    
    public String getConnectionString() {
        return "mongodb://localhost:" + port;
    }

}
