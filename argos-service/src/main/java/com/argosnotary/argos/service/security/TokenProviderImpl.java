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

import com.argosnotary.argos.service.domain.security.TokenInfo;
import com.argosnotary.argos.service.domain.security.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenProviderImpl implements TokenProvider {

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("#{T(java.time.Duration).parse('${jwt.token.refreshInterval}')}")
    private Duration refreshInterval;

    @Value("#{T(java.time.Duration).parse('${jwt.token.sessionTimout}')}")
    private Duration sessionTimeout;

    @Value("#{T(java.time.Duration).parse('${jwt.token.expiration}')}")
    private Duration expiration;

    private SecretKey secretKey;

    /**
     * create secret for application.yml
     * jwt:
     * token:
     * secret: generated secret
     *
     * @param args not used
     */
    public static void main(String[] args) {
        log.info(Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded()));
    }

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }

    @Override
    public Optional<String> refreshToken(TokenInfo tokenInfo) {
        if (!sessionExpired(tokenInfo)) {
            return Optional.of(Jwts.builder()
                    .setSubject(tokenInfo.getAccountId())
                    .setId(tokenInfo.getSessionId())
                    .setIssuedAt(new Date())
                    .setExpiration(tokenInfo.getExpiration())
                    .signWith(secretKey)
                    .compact());
        } else {
            return Optional.empty();
        }
    }

    public boolean shouldRefresh(TokenInfo tokenInfo) {
        return new Date().getTime() > tokenInfo.getIssuedAt().getTime() + refreshInterval.toMillis();
    }

    public boolean sessionExpired(TokenInfo tokenInfo) {
        return new Date().getTime() > tokenInfo.getIssuedAt().getTime() + refreshInterval.toMillis() + sessionTimeout.toMillis();
    }

    public String createToken(String accountId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(accountId)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration.toMillis()))
                .signWith(secretKey)
                .compact();
    }

    public TokenInfo getTokenInfo(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return TokenInfo.builder().accountId(claims.getSubject()).sessionId(claims.getId()).expiration(claims.getExpiration()).issuedAt(claims.getIssuedAt()).build();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.debug("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    private LocalDateTime toLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

}
