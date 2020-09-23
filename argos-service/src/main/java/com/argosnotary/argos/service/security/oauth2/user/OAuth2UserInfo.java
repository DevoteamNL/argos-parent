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
package com.argosnotary.argos.service.security.oauth2.user;

import com.argosnotary.argos.service.domain.security.oauth.OAuth2Providers;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;


public class OAuth2UserInfo {

    private Map<String, Object> attributes;
    private String providerName;
    private OAuth2Providers.OAuth2Provider oauth2Provider;

    public OAuth2UserInfo(String providerName, Map<String, Object> attributes, OAuth2Providers.OAuth2Provider oauth2Provider) {
        this.attributes = attributes;
        this.providerName = providerName;
        this.oauth2Provider = oauth2Provider;
    }

    public String getProviderName() {
        return providerName;
    }

    @NotNull
    @Size(min = 1)
    public String getId() {
        return (String) attributes
                .getOrDefault(oauth2Provider
                        .getUserIdAttribute(), null);
    }

    @NotNull
    @Size(min = 1)
    public String getName() {
        return (String) attributes
                .getOrDefault(oauth2Provider
                        .getUserNameAttribute(), null);

    }

    @Email
    @NotNull
    public String getEmail() {
        return (String) attributes
                .getOrDefault(oauth2Provider
                        .getUserEmailAttribute(), null);

    }
}
