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

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

@Getter
public class Properties {

    private final String apiBaseUrl;
    private static Properties INSTANCE;
    private final String jenkinsBaseUrl;
    private final String integrationTestServiceBaseUrl;
    private final String nexusWarSnapshotUrl;
    private final String argosTestAppBranch;
    private final String oauthStubUrl;
    private final String oauthStubPort;


    public static Properties getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Properties();
        }
        return INSTANCE;
    }

    private Properties() {
        Config conf = ConfigFactory.load();
        apiBaseUrl = conf.getString("argos-service.rest-api.base-url");
        jenkinsBaseUrl = conf.getString("jenkins.base-url");
        integrationTestServiceBaseUrl = conf.getString("argos-integration-test-service.rest-api.base-url");
        nexusWarSnapshotUrl = conf.getString("nexus.war-snapshot-url");
        argosTestAppBranch = conf.getString("argos-test-app.branch");
        oauthStubUrl = conf.getString("argos-oauth-stub.rest-api.base-url");
        oauthStubPort = conf.getString("argos-oauth-stub.rest-api.port");

    }
}

