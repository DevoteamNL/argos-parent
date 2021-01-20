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
package com.argosnotary.argos.service.adapter.in.rest;

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.SignatureValidator;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.service.domain.account.AccountService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SignatureValidatorService {

    private final AccountService accountService;

    public void validateSignature(Layout layout, Signature signature) {
    	try {
			if (!SignatureValidator.isValid(layout, signature, getPublicKey(signature))) {
			    throwInValidSignatureException();
			}
		} catch (GeneralSecurityException | IOException e) {
		    throwInValidSignatureException();
		}
    }

    public void validateSignature(Link link, Signature signature) {
        try {
			if (!SignatureValidator.isValid(link, signature, getPublicKey(signature))) {
			    throwInValidSignatureException();
			}
		} catch (GeneralSecurityException | IOException e) {
		    throwInValidSignatureException();
		}
    }

    private void throwInValidSignatureException() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid signature");
    }

    private PublicKey getPublicKey(Signature signature) throws GeneralSecurityException, IOException {
    	KeyPair keyPair = accountService.findKeyPairByKeyId(signature.getKeyId())
    	        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "signature with keyId [" + signature.getKeyId() + "] not found"));
    	return com.argosnotary.argos.domain.crypto.PublicKey.instance(keyPair.getPublicKey());
    }

}
