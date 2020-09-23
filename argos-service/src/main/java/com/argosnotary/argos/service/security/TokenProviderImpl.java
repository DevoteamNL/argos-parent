/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenProviderImpl implements TokenProvider {

    @Value("${jwt.token.secret}")
    private String secret;

    @Value("#{T(java.time.Duration).parse('${jwt.token.expiration}')}")
    private Duration timeout;

    @Value("#{T(java.time.Duration).parse('${jwt.token.sessionTimout}')}")
    private Duration sessionTimeout;

    @Value("#{T(java.time.Duration).parse('${jwt.token.refreshInterval}')}")
    private Duration refreshInterval;

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
        return LocalDateTime.now().isAfter(toLocalDateTime(tokenInfo.getIssuedAt()).plus(refreshInterval));
    }

    public boolean sessionExpired(TokenInfo tokenInfo) {
        return LocalDateTime.now().isAfter(toLocalDateTime(tokenInfo.getIssuedAt()).plus(refreshInterval).plus(sessionTimeout));
    }

    public String createToken(String accountId) {
        return Jwts.builder()
                .setSubject(accountId)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(Timestamp.valueOf(LocalDateTime.now().plus(timeout)))
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
