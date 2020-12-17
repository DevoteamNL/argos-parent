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
package com.argosnotary.argos.service.domain.security.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties("spring.security.oauth2.client")
@Getter
@Setter
public class OAuth2Providers {
    private Map<String, OAuth2Provider> provider;

    @Getter
    @Setter
    public static class OAuth2Provider {
        private String userNameAttribute;
        private String userIdAttribute;
        private String userEmailAttribute;
        private String iconUrl;
        private String displayName;
        private EmailAddressHandler emailAddressHandler;
    }

    @Getter
    @Setter
    public static class EmailAddressHandler {
        private String className;
        private String uri;
    }

}



