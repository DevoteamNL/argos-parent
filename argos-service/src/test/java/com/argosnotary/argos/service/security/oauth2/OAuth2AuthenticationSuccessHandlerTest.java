/*
 * Copyright (C) 2020 Argos Notary
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