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
package com.argosnotary.argos.integrationtest.service;

import com.argosnotary.argos.service.adapter.out.mongodb.MongoConfig;
import com.argosnotary.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl;
import com.argosnotary.argos.service.domain.account.AccountService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = {MongoConfig.class, TestITService.class, AccountService.class, ServiceAccountRepositoryImpl.class, AccountSecurityContextMock.class})
public class ArgosIntegrationtestServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArgosIntegrationtestServiceApplication.class, args);
    }
}
