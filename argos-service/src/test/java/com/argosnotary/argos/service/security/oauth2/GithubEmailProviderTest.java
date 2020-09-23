/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
package com.argosnotary.argos.service.security.oauth2;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.argosnotary.argos.domain.ArgosError;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GithubEmailProviderTest {

    private GithubEmailProvider provider;

    private WireMockServer wireMockServer;
    private Integer randomPort;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        randomPort = findRandomPort();
        wireMockServer = new WireMockServer(randomPort);
        wireMockServer.start();
        provider = new GithubEmailProvider();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    @Test
    void getEmailAddress() {
        wireMockServer.stubFor(get(urlEqualTo("/emails"))
                .willReturn(ok().withHeader("Content-Type", "application/vnd.github.v3+json").withBody("[\n" +
                        "  {\n" +
                        "    \"email\": \"octocat@github.com\",\n" +
                        "    \"verified\": true,\n" +
                        "    \"primary\": true,\n" +
                        "    \"visibility\": \"public\"\n" +
                        "  }\n" +
                        "]")));
        assertThat(provider.getEmailAddress("token", "http://localhost:" + randomPort + "/emails"), is("octocat@github.com"));
    }

    @Test
    void getEmailAddressEmptyList() {
        wireMockServer.stubFor(get(urlEqualTo("/emails"))
                .willReturn(ok().withHeader("Content-Type", "application/vnd.github.v3+json").withBody("[]")));
        Exception exception = assertThrows(ArgosError.class, () -> provider.getEmailAddress("token", "http://localhost:" + randomPort + "/emails"));
        assertThat(exception.getMessage(), is("no email"));
    }

    private static Integer findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

}