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

