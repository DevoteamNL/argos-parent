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
package com.argosnotary.argos.service.security.oauth2;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class ArgosOAuth2UserTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String AZURE = "azure";
    private static final String USER_PRINCIPAL_NAME = "userPrincipalName";
    private static final String EMAIL = "email@email.com";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PROVIDER_ID = "providerId";
    private static final String ID = "id";
    private static final Map<String, Object> ATTRIBUTES = Map.of(USER_PRINCIPAL_NAME, EMAIL, DISPLAY_NAME, DISPLAY_NAME, ID, PROVIDER_ID);
    
    
    @Mock
    private OAuth2User oAuth2User;
    
    private ArgosOAuth2User user;

    @BeforeEach
    void setUp() throws Exception {
        user = new ArgosOAuth2User(oAuth2User, ACCOUNT_ID);
    }

    @Test
    void getAttributeTest() {
        when(oAuth2User.getAttributes()).thenReturn(ATTRIBUTES);
        when(oAuth2User.getAttribute("id")).thenReturn(ACCOUNT_ID);
        when(oAuth2User.getName()).thenReturn(USER_PRINCIPAL_NAME);
        assertThat(user.getAccountId(), is(ACCOUNT_ID));
        assertThat(user.getAttribute("id"), is(ACCOUNT_ID));
        assertThat(user.getAttributes(), is(ATTRIBUTES));
        assertThat(user.getName(), is(USER_PRINCIPAL_NAME));
        
    }

}
