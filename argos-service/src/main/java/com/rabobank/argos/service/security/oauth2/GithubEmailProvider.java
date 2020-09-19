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
package com.rabobank.argos.service.security.oauth2;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.service.domain.security.oauth.EmailAddressHandler;
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

@Component("com.rabobank.argos.service.security.oauth2.GithubEmailProvider")
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
