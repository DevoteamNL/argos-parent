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

import com.argosnotary.argos.service.security.TokenProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private TokenProviderImpl tokenProvider;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    private OAuth2AuthenticationSuccessHandler successHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private ArgosOAuth2User argosOAuth2User;


    @BeforeEach
    void setUp() {
        successHandler = new OAuth2AuthenticationSuccessHandler(tokenProvider, httpCookieOAuth2AuthorizationRequestRepository);
        ReflectionTestUtils.setField(successHandler, "frontendRedirectBasePath", URI.create("https://host:89"));
    }

    @Test
    void onAuthenticationSuccess() throws IOException {
        when(authentication.getPrincipal()).thenReturn(argosOAuth2User);
        when(argosOAuth2User.getAccountId()).thenReturn("accountId");
        when(tokenProvider.createToken(eq("accountId"))).thenReturn("token");
        when(httpCookieOAuth2AuthorizationRequestRepository.getRedirectUri(request)).thenReturn(Optional.of("http://notused/uri?someExtraParam=extra"));
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verify(response).encodeRedirectURL("https://host:89/uri?someExtraParam=extra&token=token");
        verify(httpCookieOAuth2AuthorizationRequestRepository).removeAuthorizationRequestCookies(request, response);
        verify(request).getSession(false);

    }

    @Test
    void onAuthenticationIsCommitted() throws IOException {
        when(authentication.getPrincipal()).thenReturn(argosOAuth2User);
        when(argosOAuth2User.getAccountId()).thenReturn("accountId");
        when(response.isCommitted()).thenReturn(true);
        successHandler.onAuthenticationSuccess(request, response, authentication);
        verifyNoMoreInteractions(response);
    }
}