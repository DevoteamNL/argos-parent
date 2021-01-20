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

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.service.domain.security.oauth.EmailAddressHandler;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component("com.argosnotary.argos.service.security.oauth2.GithubEmailProvider")
public class GithubEmailProvider implements EmailAddressHandler {
    @Override
    public String getEmailAddress(String token, String emailUri) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.valueOf("application/vnd.github.v3+json")));
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<GithubEmailInfo>> response = restTemplate.exchange(emailUri, HttpMethod.GET, entity,
                new ParameterizedTypeReference<>() {
                });
        return Optional.ofNullable(response.getBody())
                .map(List::stream).flatMap(Stream::findFirst)
                .map(GithubEmailInfo::getEmail)
                .orElseThrow(() -> new ArgosError("no email"));
    }
}
