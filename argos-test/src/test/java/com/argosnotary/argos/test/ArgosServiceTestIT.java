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
package com.argosnotary.argos.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intuit.karate.KarateOptions;
import com.intuit.karate.junit5.Karate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import static com.argosnotary.argos.test.ServiceStatusHelper.waitForArgosIntegrationTestServiceToStart;
import static com.argosnotary.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.argosnotary.argos.test.TestServiceHelper.createDefaultTestData;

@Slf4j
@KarateOptions(tags = {"~@ignore"})
class ArgosServiceTestIT {

    private static final String SERVER_BASEURL = "server.baseurl";
    private static final String SERVER_INTEGRATION_TEST_BASEURL = "server.integration-test-service.baseurl";
    private static final String DEFAULT_TESTDATA = "default-testdata";
    private static Properties properties = Properties.getInstance();

    @BeforeAll
    static void setUp() throws JsonProcessingException {
        log.info("karate base url : {}", properties.getApiBaseUrl());
        System.setProperty(SERVER_BASEURL, properties.getApiBaseUrl());
        System.setProperty(SERVER_INTEGRATION_TEST_BASEURL, properties.getIntegrationTestServiceBaseUrl());
        waitForArgosServiceToStart();
        waitForArgosIntegrationTestServiceToStart();
        DefaultTestData defaultTestData = createDefaultTestData();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String defaultTestDataJson = objectMapper.writeValueAsString(defaultTestData);
        System.setProperty(DEFAULT_TESTDATA, defaultTestDataJson);
        log.info("default test data: {}", defaultTestDataJson);

    }

    @Karate.Test
    Karate link() {
        return new Karate().feature("classpath:feature/link/link.feature");
    }

    @Karate.Test
    Karate supplyChain() {
        return new Karate().feature("classpath:feature/supplychain/supplychain.feature");
    }

    @Karate.Test
    Karate layout() {
        return new Karate().feature("classpath:feature/layout/layout.feature");
    }

    @Karate.Test
    Karate verification() {
        return new Karate().feature("classpath:feature/verification/verification.feature");
    }

    @Karate.Test
    Karate verification2() {
        return new Karate().feature("classpath:feature/verification2.0/verification2.0.feature");
    }

    @Karate.Test
    Karate release() {
        return new Karate().feature("classpath:feature/release/release.feature");
    }

    @Karate.Test
    Karate personalaccount() {
        return new Karate().feature("classpath:feature/account/personalaccount.feature");
    }

    @Karate.Test
    Karate label() {
        return new Karate().feature("classpath:feature/label/label.feature");
    }

    @Karate.Test
    Karate hierarchy() {
        return new Karate().feature("classpath:feature/hierarchy/hierarchy.feature");
    }

    @Karate.Test
    Karate serviceAccount() {
        return new Karate().feature("classpath:feature/account/service-account.feature");
    }

    @Karate.Test
    Karate permission() {
        return new Karate().feature("classpath:feature/permission/permission.feature");
    }

    @Karate.Test()
    Karate searchAccounts() {
        return new Karate().feature("classpath:feature/account/search-account.feature");
    }

    @Karate.Test()
    Karate oauthProviders() {
        return new Karate().feature("classpath:feature/oauthprovider/oauthprovider.feature");
    }
}
