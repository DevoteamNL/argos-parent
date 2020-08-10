/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.crypto.KeyPair;
import com.rabobank.argos.domain.crypto.PublicKeyFactory;
import com.rabobank.argos.domain.crypto.Signature;
import com.rabobank.argos.domain.crypto.signing.SignatureValidator;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.account.AccountService;
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
    	return PublicKeyFactory.instance(keyPair.getPublicKey());
    }

}
