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

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.security.oauth.EmailAddressHandler;
import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers;
import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers.OAuth2Provider;
import com.argosnotary.argos.service.security.oauth2.user.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AccountService accountService;
    private final OAuth2Providers auth2Providers;
    private final ApplicationContext applicationContext;
    private final DefaultOAuth2UserService defaultOAuth2UserService = new DefaultOAuth2UserService();
    private final Validator validator;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        try {
            OAuth2User oAuth2User = defaultOAuth2UserService.loadUser(oAuth2UserRequest);
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (ArgosError ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    private ArgosOAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        if (oAuth2User.getAttributes() == null || oAuth2User.getAttributes().isEmpty()) {
            throw new ArgosError("invalid response from oauth profile service");
        }

        String providerName = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        OAuth2Provider oauth2Provider = getoAuth2Provider(oAuth2UserRequest, providerName);

        Map<String, Object> attributes = handleEmailProperty(oAuth2UserRequest, oAuth2User, oauth2Provider);

        return accountService.authenticateUser(convertToPersonalAccount(new OAuth2UserInfo(providerName, attributes, oauth2Provider)))
                .map(account -> new ArgosOAuth2User(oAuth2User, account.getAccountId()))
                .orElseThrow(() -> new ArgosError("account not authenticated"));

    }

    private OAuth2Provider getoAuth2Provider(OAuth2UserRequest oAuth2UserRequest, String providerName) {
        return Optional.ofNullable(auth2Providers
                .getProvider()
                .getOrDefault(oAuth2UserRequest.getClientRegistration().getRegistrationId(), null))
                .orElseThrow(() -> new ArgosError("no provider is configured for: " + providerName));
    }

    private Map<String, Object> handleEmailProperty(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User, OAuth2Provider oauth2Provider) {
        return Optional.ofNullable(oauth2Provider.getEmailAddressHandler())
                .map(handler -> applicationContext.getBean(handler.getClassName(), EmailAddressHandler.class)
                        .getEmailAddress(oAuth2UserRequest.getAccessToken().getTokenValue(), handler.getUri())).map(email -> {
                    Map<String, Object> stringObjectMap = new HashMap<>(oAuth2User.getAttributes());
                    stringObjectMap.put(oauth2Provider.getUserEmailAttribute(), email);
                    return stringObjectMap;
                }).orElse(oAuth2User.getAttributes());
    }

    private PersonalAccount convertToPersonalAccount(OAuth2UserInfo oAuth2UserInfo) {
        Set<ConstraintViolation<OAuth2UserInfo>> violations = validator.validate(oAuth2UserInfo);
        if (violations.isEmpty()) {
            return PersonalAccount.builder()
                    .name(oAuth2UserInfo.getName())
                    .email(oAuth2UserInfo.getEmail())
                    .providerId(oAuth2UserInfo.getId())
                    .providerName(oAuth2UserInfo.getProviderName())
                    .build();
        } else {
            throw new ArgosError(violations.stream().map(violation -> violation.getPropertyPath().toString() + " : " + violation.getMessage()).collect(Collectors.joining(", ")));
        }
    }


}
