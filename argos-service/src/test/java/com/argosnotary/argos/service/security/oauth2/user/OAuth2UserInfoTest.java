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
package com.argosnotary.argos.service.security.oauth2.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.empty;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers.OAuth2Provider;

class OAuth2UserInfoTest {
    
    private OAuth2UserInfo userInfo;
    
    private OAuth2Provider oauth2Provider;
    
    private Map<String, Object> attributes;
    
    private Validator validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        oauth2Provider = new OAuth2Provider();
        oauth2Provider.setEmailAddressHandler(null);
        oauth2Provider.setUserEmailAttribute("userEmailAttribute");
        oauth2Provider.setUserIdAttribute("userIdAttribute");
        oauth2Provider.setUserNameAttribute("userNameAttribute");
        
        attributes = new HashMap<>();
        
        attributes.put("userEmailAttribute", "user@emailattribute.value");
        attributes.put("userIdAttribute", "userIdAttributeValue");
        attributes.put("userNameAttribute", "userNameAttributeValue");
        
        userInfo =  new OAuth2UserInfo("providerName", attributes, oauth2Provider);
    }

    @Test
    void getIdTest() {
        assertThat(userInfo.getId(), is("userIdAttributeValue"));
        Set<ConstraintViolation<OAuth2UserInfo>> violations = validator.validate(userInfo);
        assertThat(violations, empty());
        attributes.put("userIdAttribute", null);
        assertThat(userInfo.getId(), nullValue());
        violations = validator.validate(userInfo);
        assertThat(violations.iterator().next().getMessage(), is("must not be null"));
        attributes.put("userIdAttribute", 1234);
        assertThat(userInfo.getId(), is("1234"));
        violations = validator.validate(userInfo);
        assertThat(violations, empty());
    }

    @Test
    void getNameTest() {
        assertThat(userInfo.getName(), is("userNameAttributeValue"));
        attributes.put("userNameAttribute", null);
        assertThat(userInfo.getName(), nullValue());

    }

    @Test
    void getEmailTest(){
        assertThat(userInfo.getEmail(), is("user@emailattribute.value"));
        Set<ConstraintViolation<OAuth2UserInfo>> violations = validator.validate(userInfo);
        assertThat(violations, empty());
        attributes.put("userEmailAttribute", null);
        assertThat(userInfo.getEmail(), nullValue());
        violations = validator.validate(userInfo);
        assertThat(violations.iterator().next().getMessage(), is("must not be null"));
        attributes.put("userEmailAttribute", "email");
        violations = validator.validate(userInfo);
        assertThat(violations.iterator().next().getMessage(), is("must be a well-formed email address"));

    }

}
