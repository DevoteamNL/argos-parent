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

import com.argosnotary.argos.service.domain.security.AccountUserDetailsAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;


@Slf4j
@RequiredArgsConstructor
public class PersonalAccountAuthenticationProvider implements AuthenticationProvider {

    private static final String NOT_AUTHENTICATED = "not authenticated";
    private final PersonalAccountUserDetailsService personalAccountUserDetailsService;
    private final LogContextHelper logContextHelper;

    @Override
    public Authentication authenticate(Authentication notAuthenticatedPersonalAccount) {
        PersonalAccountAuthenticationToken personalAccountAuthenticationToken = (PersonalAccountAuthenticationToken) notAuthenticatedPersonalAccount;
        try {
            AccountUserDetailsAdapter userDetails = (AccountUserDetailsAdapter) personalAccountUserDetailsService.loadUserByToken(personalAccountAuthenticationToken);
            Authentication authenticatedPersonalAccount = new PersonalAccountAuthenticationToken(personalAccountAuthenticationToken.getTokenInfo(), userDetails, userDetails.getAuthorities());
            authenticatedPersonalAccount.setAuthenticated(true);
            logContextHelper.addAccountInfoToLogContext(userDetails);
            log.debug("successfully authenticated personal account {}", userDetails.getUsername());
            return authenticatedPersonalAccount;
        } catch (Exception ex) {
            log.warn("invalid access attempt  {}", personalAccountAuthenticationToken);
            throw new BadCredentialsException(NOT_AUTHENTICATED);
        }
    }

    @Override
    public boolean supports(Class<?> authenticationTokenClass) {
        return authenticationTokenClass.equals(PersonalAccountAuthenticationToken.class);
    }
}
