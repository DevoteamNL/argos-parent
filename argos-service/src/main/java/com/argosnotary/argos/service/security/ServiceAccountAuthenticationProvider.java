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
package com.argosnotary.argos.service.security;

import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class ServiceAccountAuthenticationProvider implements AuthenticationProvider {

    private static final String NOT_AUTHENTICATED = "not authenticated";
    private final ServiceAccountUserDetailsService serviceAccountUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LogContextHelper logContextHelper;

    @Override
    public Authentication authenticate(Authentication notAuthenticatedServiceAccount) {
        ServiceAccountAuthenticationToken serviceAccountAuthenticationToken = (ServiceAccountAuthenticationToken) notAuthenticatedServiceAccount;
        try {
            AccountUserDetailsAdapter userDetails = (AccountUserDetailsAdapter) serviceAccountUserDetailsService
                    .loadUserById(serviceAccountAuthenticationToken.getServiceAccountCredentials().getKeyId());
            log.debug("successfully found service account by key id {}", userDetails.getUsername());
            String password = serviceAccountAuthenticationToken.getServiceAccountCredentials().getPassword();
            ServiceAccountKeyPair serviceAccountKeyPair = (ServiceAccountKeyPair) userDetails.getAccount().getActiveKeyPair();
            if (passwordEncoder.matches(password, serviceAccountKeyPair.getEncryptedHashedKeyPassphrase())) {
                log.debug("successfully authenticated service account {}", userDetails.getUsername());
                logContextHelper.addAccountInfoToLogContext(userDetails);
                return new ServiceAccountAuthenticationToken(serviceAccountAuthenticationToken.getServiceAccountCredentials(),
                        userDetails,
                        userDetails.getAuthorities());
            } else {
                log.warn("invalid access attempt {}", serviceAccountAuthenticationToken);
                throw new BadCredentialsException(NOT_AUTHENTICATED);
            }
        } catch (Exception ex) {
            log.warn("invalid access attempt {}", serviceAccountAuthenticationToken);
            throw new BadCredentialsException(NOT_AUTHENTICATED);
        }
    }


    @Override
    public boolean supports(Class<?> authenticationTokenClass) {
        return authenticationTokenClass.equals(ServiceAccountAuthenticationToken.class);
    }
}
