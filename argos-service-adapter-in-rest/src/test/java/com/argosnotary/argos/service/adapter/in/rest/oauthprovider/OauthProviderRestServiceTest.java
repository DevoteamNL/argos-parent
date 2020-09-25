/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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