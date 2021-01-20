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
        Object id = attributes
                .get(oauth2Provider
                        .getUserIdAttribute());
        if (id instanceof Integer) {
            return id.toString();
        }
        return (String) id;
    }

    @NotNull
    @Size(min = 1)
    public String getName() {
        return (String) attributes
                .get(oauth2Provider
                        .getUserNameAttribute());

    }

    @Email
    @NotNull
    public String getEmail() {
        return (String) attributes
                .get(oauth2Provider
                        .getUserEmailAttribute());

    }
}
