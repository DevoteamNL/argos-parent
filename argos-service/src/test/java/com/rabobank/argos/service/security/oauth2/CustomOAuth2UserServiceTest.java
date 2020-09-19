/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.security.oauth2;

import com.rabobank.argos.domain.account.PersonalAccount;
import com.rabobank.argos.service.domain.account.AccountService;
import com.rabobank.argos.service.domain.security.oauth.EmailAddressHandler;
import com.rabobank.argos.service.domain.security.oauth.OAuth2Providers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import javax.validation.Validation;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String AZURE = "azure";
    private static final String USER_PRINCIPAL_NAME = "userPrincipalName";
    private static final String EMAIL = "email@email.com";
    private static final String DISPLAY_NAME = "displayName";
    private static final String PROVIDER_ID = "providerId";
    private static final String ID = "id";
    @Mock
    private AccountService accountService;

    @Mock
    private OAuth2Providers oAuth2Providers;

    @Mock
    private OAuth2Providers.OAuth2Provider oauth2Provider;

    @Mock
    private DefaultOAuth2UserService defaultOAuth2UserService;

    @Mock
    private OAuth2UserRequest oAuth2UserRequest;

    @Mock
    private OAuth2User oAuth2User;

    @Captor
    private ArgumentCaptor<PersonalAccount> userArgumentCaptor;


    private ClientRegistration clientRegistration;

    private CustomOAuth2UserService userService;

    @Mock
    private PersonalAccount personalAccount;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private OAuth2Providers.EmailAddressHandler emailAddressHandler;

    @Mock
    private EmailAddressHandler emailHandler;

    @Mock
    private OAuth2AccessToken accessToken;

    @BeforeEach
    void setUp() {
        clientRegistration = ClientRegistration.withRegistrationId(AZURE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId("clientId")
                .redirectUriTemplate("template")
                .authorizationUri("/")
                .tokenUri("/")
                .build();
        userService = new CustomOAuth2UserService(accountService, oAuth2Providers, applicationContext, Validation.buildDefaultValidatorFactory().getValidator());
        ReflectionTestUtils.setField(userService, "defaultOAuth2UserService", defaultOAuth2UserService);
    }


    @Test
    void loadUserNewUserWithEmptyAttributesShouldThrowError() {
        when(oAuth2User.getAttributes()).thenReturn(Map.of());
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        assertThrows(InternalAuthenticationServiceException.class, () -> userService.loadUser(oAuth2UserRequest));

    }

    @Test
    void loadUserNotFoundShouldThrowError() {
        setupMocks();
        when(oauth2Provider.getUserEmailAttribute()).thenReturn(USER_PRINCIPAL_NAME);
        when(oauth2Provider.getUserIdAttribute()).thenReturn(ID);
        when(oauth2Provider.getUserNameAttribute()).thenReturn(DISPLAY_NAME);
        when(accountService.authenticateUser(any())).thenReturn(Optional.empty());
        InternalAuthenticationServiceException error = assertThrows(InternalAuthenticationServiceException.class, () -> userService.loadUser(oAuth2UserRequest));
        assertThat(error.getMessage(), is("account not authenticated"));
    }

    @Test
    void loadUserNewUser() {
        setupMocks();
        when(oauth2Provider.getUserEmailAttribute()).thenReturn(USER_PRINCIPAL_NAME);
        when(oauth2Provider.getUserNameAttribute()).thenReturn(DISPLAY_NAME);
        when(oauth2Provider.getUserIdAttribute()).thenReturn(ID);
        when(personalAccount.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountService.authenticateUser(any())).thenReturn(Optional.of(personalAccount));
        ArgosOAuth2User userPrincipal = (ArgosOAuth2User) userService.loadUser(oAuth2UserRequest);
        assertThat(userPrincipal.getAccountId(), is(ACCOUNT_ID));
        verify(accountService).authenticateUser(userArgumentCaptor.capture());
        PersonalAccount createdPersonalAccount = userArgumentCaptor.getValue();
        assertThat(createdPersonalAccount.getName(), is(DISPLAY_NAME));
        assertThat(createdPersonalAccount.getEmail(), is(EMAIL));
        assertThat(createdPersonalAccount.getProviderName(), is(AZURE));
        assertThat(createdPersonalAccount.getProviderId(), is(PROVIDER_ID));
    }

    @Test
    void loadUserNewUserWithCustomEmail() {
        setupMocks();
        when(oauth2Provider.getEmailAddressHandler()).thenReturn(emailAddressHandler);
        when(emailAddressHandler.getClassName()).thenReturn("className");
        when(emailAddressHandler.getUri()).thenReturn("email uri");
        when(applicationContext.getBean("className", EmailAddressHandler.class)).thenReturn(emailHandler);
        when(oAuth2UserRequest.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("token");
        when(emailHandler.getEmailAddress("token", "email uri")).thenReturn("other@email.com");
        when(oauth2Provider.getUserEmailAttribute()).thenReturn(USER_PRINCIPAL_NAME);
        when(oauth2Provider.getUserNameAttribute()).thenReturn(DISPLAY_NAME);
        when(oauth2Provider.getUserIdAttribute()).thenReturn(ID);
        when(personalAccount.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountService.authenticateUser(any())).thenReturn(Optional.of(personalAccount));
        ArgosOAuth2User userPrincipal = (ArgosOAuth2User) userService.loadUser(oAuth2UserRequest);
        assertThat(userPrincipal.getAccountId(), is(ACCOUNT_ID));
        verify(accountService).authenticateUser(userArgumentCaptor.capture());
        PersonalAccount createdPersonalAccount = userArgumentCaptor.getValue();
        assertThat(createdPersonalAccount.getName(), is(DISPLAY_NAME));
        assertThat(createdPersonalAccount.getEmail(), is("other@email.com"));
        assertThat(createdPersonalAccount.getProviderName(), is(AZURE));
        assertThat(createdPersonalAccount.getProviderId(), is(PROVIDER_ID));
    }

    @Test
    void loadUserNoEmailAddress() {
        setupMocks();
        when(oauth2Provider.getUserEmailAttribute()).thenReturn(USER_PRINCIPAL_NAME);
        when(oauth2Provider.getUserNameAttribute()).thenReturn(DISPLAY_NAME);
        when(oauth2Provider.getUserIdAttribute()).thenReturn(ID);
        when(oAuth2User.getAttributes()).thenReturn(Map.of(DISPLAY_NAME, DISPLAY_NAME, ID, PROVIDER_ID));
        InternalAuthenticationServiceException serviceException = assertThrows(InternalAuthenticationServiceException.class, () -> userService.loadUser(oAuth2UserRequest));
        assertThat(serviceException.getCause().getMessage(), is("email : must not be null"));
    }

    @Test
    void loadUserNoOAuthProvider() {
        when(oAuth2Providers.getProvider()).thenReturn(Map.of("git", oauth2Provider));
        when(oAuth2User.getAttributes()).thenReturn(Map.of(USER_PRINCIPAL_NAME, USER_PRINCIPAL_NAME, DISPLAY_NAME, DISPLAY_NAME, ID, PROVIDER_ID));
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        InternalAuthenticationServiceException serviceException = assertThrows(InternalAuthenticationServiceException.class, () -> userService.loadUser(oAuth2UserRequest));
        assertThat(serviceException.getCause().getMessage(), is("no provider is configured for: azure"));
    }

    private void setupMocks() {
        when(oAuth2Providers.getProvider()).thenReturn(Map.of(AZURE, oauth2Provider));
        when(oAuth2User.getAttributes()).thenReturn(Map.of(USER_PRINCIPAL_NAME, EMAIL, DISPLAY_NAME, DISPLAY_NAME, ID, PROVIDER_ID));
        when(defaultOAuth2UserService.loadUser(oAuth2UserRequest)).thenReturn(oAuth2User);
        when(oAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);
    }

}