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

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestOAuthProvider;
import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers;
import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers.OAuth2Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OauthProviderRestServiceTest {
    protected static final String DISPLAY_NAME = "displayName";
    protected static final String AZURE = "azure";
    protected static final String ICON_URL = "iconUrl";
    @Mock
    private OAuth2Providers oAuthProviders;
    private OAuth2Provider oAuth2Provider;
    private OauthProviderRestService oauthProviderRestService;

    @BeforeEach
    void setUp() {
        oAuth2Provider = new OAuth2Provider();
        oAuth2Provider.setDisplayName(DISPLAY_NAME);
        oAuth2Provider.setIconUrl(ICON_URL);
        oauthProviderRestService = new OauthProviderRestService(oAuthProviders);
    }

    @Test
    void getOAuthProviders() {
        when(oAuthProviders.getProvider()).thenReturn(Map.of(AZURE, oAuth2Provider));
        ResponseEntity<List<RestOAuthProvider>> responseEntity = oauthProviderRestService.getOAuthProviders();
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), is(notNullValue()));
        assertThat(responseEntity.getBody(), is(hasSize(1)));
        assertThat(responseEntity.getBody().get(0).getDisplayName(), is(DISPLAY_NAME));
        assertThat(responseEntity.getBody().get(0).getProviderName(), is(AZURE));
        assertThat(responseEntity.getBody().get(0).getIconUrl(), is(ICON_URL));
    }
}