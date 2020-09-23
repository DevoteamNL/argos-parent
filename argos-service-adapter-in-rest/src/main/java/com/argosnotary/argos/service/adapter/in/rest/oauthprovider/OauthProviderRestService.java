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
package com.argosnotary.argos.service.adapter.in.rest.oauthprovider;

import com.argosnotary.argos.service.adapter.in.rest.api.handler.OauthProviderApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestOAuthProvider;
import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OauthProviderRestService implements OauthProviderApi {
    private final OAuth2Providers oAuthProviders;

    @Override
    public ResponseEntity<List<RestOAuthProvider>> getOAuthProviders() {
        List<RestOAuthProvider> restOAuthProviders = oAuthProviders
                .getProvider()
                .entrySet()
                .stream()
                .map(p -> new RestOAuthProvider()
                        .providerName(p.getKey())
                        .displayName(p.getValue().getDisplayName())
                        .iconUrl(p.getValue().getIconUrl())).collect(Collectors.toList());

        return ResponseEntity.ok(restOAuthProviders);
    }
}

