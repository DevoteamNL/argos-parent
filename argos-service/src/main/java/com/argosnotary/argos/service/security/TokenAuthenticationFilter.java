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
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestError;
import com.argosnotary.argos.service.domain.account.FinishedSessionRepository;
import com.argosnotary.argos.service.domain.security.TokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProviderImpl tokenProvider;
    private final FinishedSessionRepository finishedSessionRepository;
    private final ObjectMapper mapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<TokenInfo> optionalTokenInfo = getJwtFromRequest(request).filter(tokenProvider::validateToken).map(tokenProvider::getTokenInfo);
        if (optionalTokenInfo.isPresent()) {
            TokenInfo tokenInfo = optionalTokenInfo.get();
            if (!tokenProvider.sessionExpired(tokenInfo) && !finishedSessionRepository.isUsedSessionId(tokenInfo.getSessionId())) {
                if (!"/api/personalaccount/me/refresh".equals(request.getRequestURI()) && tokenProvider.shouldRefresh(tokenInfo)) {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    mapper.writeValue(response.getWriter(), new RestError().message("refresh token"));
                } else {
                    PersonalAccountAuthenticationToken authentication = new PersonalAccountAuthenticationToken(tokenInfo, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("successfully resolved bearer token for account {}", tokenInfo.getAccountId());
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }

    private Optional<String> getJwtFromRequest(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(bearerToken -> bearerToken.startsWith("Bearer "))
                .map(bearerToken -> bearerToken.substring(7));

    }
}
