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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeyIdBasicAuthenticationFilterTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private BasicAuthenticationConverter basicAuthenticationConverter;

    private KeyIdBasicAuthenticationFilter keyIdBasicAuthenticationFilter;

    private UsernamePasswordAuthenticationToken basicAuthPwToken = new UsernamePasswordAuthenticationToken("keyId", "pw", Collections.emptyList());

    @BeforeEach
    void setup() {
        keyIdBasicAuthenticationFilter = new KeyIdBasicAuthenticationFilter(basicAuthenticationConverter);
    }

    @Test
    void doFilterInternalWithValidBasicHeader() throws IOException, ServletException {
        when(basicAuthenticationConverter.convert(request))
                .thenReturn(basicAuthPwToken);
        keyIdBasicAuthenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, instanceOf(ServiceAccountAuthenticationToken.class));
        ServiceAccountAuthenticationToken serviceAccountAuthenticationToken = (ServiceAccountAuthenticationToken) authentication;
        assertThat(serviceAccountAuthenticationToken.getServiceAccountCredentials().getKeyId(), is("keyId"));
        assertThat(serviceAccountAuthenticationToken.getServiceAccountCredentials().getPassword(), is("pw"));
        assertThat(serviceAccountAuthenticationToken.getDetails(), notNullValue());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternalWithInvalidValidBasicHeader() throws IOException, ServletException {
        when(basicAuthenticationConverter.convert(request))
                .thenReturn(null);
        keyIdBasicAuthenticationFilter.doFilterInternal(request, response, filterChain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication, nullValue());
        verify(filterChain).doFilter(request, response);
    }
}