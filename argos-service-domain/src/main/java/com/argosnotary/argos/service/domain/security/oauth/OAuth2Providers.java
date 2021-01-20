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



