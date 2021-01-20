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

import com.argosnotary.argos.service.security.CookieHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpCookieOAuth2AuthorizationRequestRepositoryTest {

    @Mock
    private CookieHelper cookieHelper;

    private HttpCookieOAuth2AuthorizationRequestRepository repository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private OAuth2AuthorizationRequest oAuth2AuthorizationRequest;

    @Mock
    private Cookie cookie;

    @BeforeEach
    void setUp() {
        oAuth2AuthorizationRequest = OAuth2AuthorizationRequest.authorizationCode().authorizationUri("http://some").clientId("is").build();
        repository = new HttpCookieOAuth2AuthorizationRequestRepository(cookieHelper);
    }

    @Test
    void loadAuthorizationRequestFound() {
        when(cookieHelper.getCookieValueAsObject(request, "oauth2_auth_request", OAuth2AuthorizationRequest.class)).thenReturn(Optional.of(oAuth2AuthorizationRequest));
        assertThat(repository.loadAuthorizationRequest(request), sameInstance(oAuth2AuthorizationRequest));
    }

    @Test
    void loadAuthorizationRequestNotFound() {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = repository.loadAuthorizationRequest(request);
        assertThat(oAuth2AuthorizationRequest == null, is(true));
    }

    @Test
    void saveAuthorizationRequestNull() {
        repository.saveAuthorizationRequest(null, request, response);
        verify(cookieHelper).deleteCookie(request, response, "oauth2_auth_request");
        verify(cookieHelper).deleteCookie(request, response, "redirect_uri");
    }

    @Test
    void saveAuthorizationRequestNotNull() {
        when(request.getParameter("redirect_uri")).thenReturn("url");
        repository.saveAuthorizationRequest(oAuth2AuthorizationRequest, request, response);
        verify(cookieHelper).addCookie(response, "oauth2_auth_request", oAuth2AuthorizationRequest, 180);
        verify(cookieHelper).addCookie(response, "redirect_uri", "url", 180);
    }

    @Test
    void removeAuthorizationRequest() {
        when(cookieHelper.getCookieValueAsObject(request, "oauth2_auth_request", OAuth2AuthorizationRequest.class)).thenReturn(Optional.of(oAuth2AuthorizationRequest));
        assertThat(repository.removeAuthorizationRequest(request), sameInstance(oAuth2AuthorizationRequest));
    }


    @Test
    void getRedirectUri() {
        when(cookie.getValue()).thenReturn("value");
        when(cookieHelper.getCookie(request, "redirect_uri")).thenReturn(Optional.of(cookie));
        assertThat(repository.getRedirectUri(request), is(Optional.of("value")));
    }
}