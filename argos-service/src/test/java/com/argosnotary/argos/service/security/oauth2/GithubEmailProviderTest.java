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