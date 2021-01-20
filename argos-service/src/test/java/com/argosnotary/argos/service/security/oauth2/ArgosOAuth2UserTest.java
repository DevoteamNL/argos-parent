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
