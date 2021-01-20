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
package com.argosnotary.argos.service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.service.domain.account.FinishedSessionRepository;
import com.argosnotary.argos.service.domain.security.TokenInfo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    @Mock
    private TokenProviderImpl tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private TokenInfo tokenInfo;

    private TokenAuthenticationFilter filter;

    @Mock
    private FinishedSessionRepository finishedSessionRepository;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(null);
        filter = new TokenAuthenticationFilter(tokenProvider, finishedSessionRepository, new ObjectMapper());
    }

    @Test
    @SneakyThrows
    void doFilterInternal() {
        when(tokenProvider.getTokenInfo("jwtToken")).thenReturn(tokenInfo);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(tokenProvider.validateToken("jwtToken")).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getDetails(), is(notNullValue()));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @SneakyThrows
    void doFilterInternalNotValidJwt() {
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(tokenProvider.validateToken("jwtToken")).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
    }

    @Test
    @SneakyThrows
    void doFilterInternalWrongBearer() {
        when(request.getHeader("Authorization")).thenReturn("bearer jwtToken");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
    }

    @Test
    @SneakyThrows
    void doFilterInternalNoAuthorization() {
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
    }

    @Test
    @SneakyThrows
    void doFilterInternalShouldRefresh() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        when(tokenProvider.getTokenInfo("jwtToken")).thenReturn(tokenInfo);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(tokenProvider.validateToken("jwtToken")).thenReturn(true);
        when(tokenProvider.shouldRefresh(tokenInfo)).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
        verifyNoInteractions(filterChain);
        verify(response).setContentType("application/json");
        verify(response).setStatus(401);
        assertThat(stringWriter.toString(), is("{\"message\":\"refresh token\"}"));
    }

    @Test
    @SneakyThrows
    void doFilterInternalShouldRefreshAllowForRefreshEndpoint() {
        when(request.getRequestURI()).thenReturn("/api/personalaccount/me/refresh");
        when(tokenProvider.getTokenInfo("jwtToken")).thenReturn(tokenInfo);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(tokenProvider.validateToken("jwtToken")).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getDetails(), is(notNullValue()));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @SneakyThrows
    void doFilterInternalSessionExpired() {
        when(tokenProvider.getTokenInfo("jwtToken")).thenReturn(tokenInfo);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwtToken");
        when(tokenProvider.validateToken("jwtToken")).thenReturn(true);
        when(tokenProvider.sessionExpired(tokenInfo)).thenReturn(true);
        filter.doFilterInternal(request, response, filterChain);
        assertThat(SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
        verify(filterChain).doFilter(request, response);
    }

}