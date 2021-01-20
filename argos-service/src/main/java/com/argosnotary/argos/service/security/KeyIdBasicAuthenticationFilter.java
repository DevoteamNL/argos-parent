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

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.argosnotary.argos.service.security.ServiceAccountAuthenticationToken.ServiceAccountCredentials;

@Slf4j
public class KeyIdBasicAuthenticationFilter extends OncePerRequestFilter {
    private final BasicAuthenticationConverter authenticationConverter;

    public KeyIdBasicAuthenticationFilter(BasicAuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        UsernamePasswordAuthenticationToken parsedTokenFromBasicHeader = authenticationConverter.convert(request);

        if (parsedTokenFromBasicHeader != null) {
            ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials
                    .builder()
                    .keyId((String) parsedTokenFromBasicHeader.getPrincipal())
                    .password((String) parsedTokenFromBasicHeader.getCredentials())
                    .build();

            ServiceAccountAuthenticationToken serviceAccountAuthenticationToken = new ServiceAccountAuthenticationToken(serviceAccountCredentials
                    , null);
            serviceAccountAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(serviceAccountAuthenticationToken);
            log.debug("successfully resolved basic token  {}", parsedTokenFromBasicHeader);
        }

        chain.doFilter(request, response);
    }

}
