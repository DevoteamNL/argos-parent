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
package com.argosnotary.argos.service.security;

import com.argosnotary.argos.service.domain.security.TokenInfo;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenProviderImplTest {

    public static final String SECRET = "Zrf3tmpRczYszNFS92mFC/JEuxiwhRAe5fO/GdbqL2g9wa2V7bi0VKRuy/VantPuzN/xo43t36zZUGgJNdjD9w==";
    private TokenProviderImpl tokenProvider;

    @Mock
    private TokenInfo tokenInfo;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProviderImpl();
        ReflectionTestUtils.setField(tokenProvider, "secret", SECRET);
        ReflectionTestUtils.setField(tokenProvider, "timeout", Duration.of(1, MINUTES));
        ReflectionTestUtils.setField(tokenProvider, "sessionTimeout", Duration.of(2, MINUTES));
        ReflectionTestUtils.setField(tokenProvider, "refreshInterval", Duration.of(3, MINUTES));
        tokenProvider.init();
    }

    @Test
    void createToken() {
        TokenProviderImpl.main(new String[]{});
        String token = tokenProvider.createToken("id");
        assertThat(tokenProvider.validateToken(token), is(true));
        assertThat(tokenProvider.getTokenInfo(token).getAccountId(), is("id"));
        assertThat(tokenProvider.getTokenInfo(token).getSessionId(), hasLength(36));
    }

    @Test
    void validateTokenInCorrectJwt() {
        assertThat(tokenProvider.validateToken("incorrect"), is(false));
    }

    @Test
    void validateTokenWrongKey() {
        String token = tokenProvider.createToken("id");
        ReflectionTestUtils.setField(tokenProvider, "secretKey", Keys.secretKeyFor(SignatureAlgorithm.HS512));
        assertThat(tokenProvider.validateToken(token), is(false));
    }

    @Test
    void validateTokenExpired() {
        ReflectionTestUtils.setField(tokenProvider, "timeout", Duration.of(1, ChronoUnit.NANOS));
        String token = tokenProvider.createToken("id");
        assertThat(tokenProvider.validateToken(token), is(false));
    }

    @Test
    void refresh() {
        when(tokenInfo.getSessionId()).thenReturn("sessionId");
        when(tokenInfo.getAccountId()).thenReturn("accountId");
        Date expDate = Date.from(LocalDateTime.now().plus(Duration.of(10, SECONDS)).atZone(ZoneId.systemDefault()).toInstant());
        when(tokenInfo.getExpiration()).thenReturn(expDate);
        when(tokenInfo.getIssuedAt()).thenReturn(new Date());
        Optional<String> optionalToken = tokenProvider.refreshToken(tokenInfo);
        String token = optionalToken.get();
        assertThat(tokenProvider.validateToken(token), is(true));
        TokenInfo tokenInfo = tokenProvider.getTokenInfo(token);
        assertThat(tokenInfo.getAccountId(), is("accountId"));
        assertThat(tokenInfo.getSessionId(), is("sessionId"));
        assertThat(tokenInfo.getExpiration().toString(), is(expDate.toString()));
    }

    @Test
    void refreshExpired() {
        Date issuedAt = Date.from(LocalDateTime.now().minus(Duration.of(5, MINUTES)).atZone(ZoneId.systemDefault()).toInstant());
        when(tokenInfo.getIssuedAt()).thenReturn(issuedAt);
        assertThat(tokenProvider.refreshToken(tokenInfo).isPresent(), is(false));
    }

    @Test
    void shouldRefresh() {
        Date issuedAt = Date.from(LocalDateTime.now().minus(Duration.of(3, MINUTES)).atZone(ZoneId.systemDefault()).toInstant());
        when(tokenInfo.getIssuedAt()).thenReturn(issuedAt);
        assertThat(tokenProvider.shouldRefresh(tokenInfo), is(true));
    }

    @Test
    void shouldNotRefresh() {
        Date issuedAt = Date.from(LocalDateTime.now().minus(Duration.of(3, MINUTES)).plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant());
        when(tokenInfo.getIssuedAt()).thenReturn(issuedAt);
        assertThat(tokenProvider.shouldRefresh(tokenInfo), is(false));
    }
}