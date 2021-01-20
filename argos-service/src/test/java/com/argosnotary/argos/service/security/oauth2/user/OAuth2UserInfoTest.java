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
