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
package com.argosnotary.argos.service.domain.verification;

import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.SignatureValidator;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Optional;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.LAYOUT_METABLOCK_SIGNATURE;

@Component
@Slf4j
@RequiredArgsConstructor
public class LayoutMetaBlockSignatureVerification implements Verification {

    @Override
    public Priority getPriority() {
        return LAYOUT_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return verify(context.getLayoutMetaBlock());
    }

    private VerificationRunResult verify(LayoutMetaBlock layoutMetaBlock) {
        boolean isValid = layoutMetaBlock
                .getSignatures()
                .stream()
                .allMatch(signature -> isValidSignature(signature, layoutMetaBlock.getLayout()));
                        
        if (!isValid) {
            log.info("failed LayoutMetaBlockSignatureVerification");
        }
        return VerificationRunResult.builder()
                .runIsValid(isValid)
                .build();

    }
    
    private boolean isValidSignature(Signature signature, Layout layout) {
        Optional<PublicKey> publicKey = getPublicKey(layout, signature.getKeyId());
        if (publicKey.isEmpty()) {
            log.info("Public Key with id [{}] is not avaiable in the layout.", signature.getKeyId());
            return false;
        }
        if (!SignatureValidator.isValid(layout, signature, publicKey.get())) {
            log.info("Signature of layout with keyId [{}] is not valid.", signature.getKeyId());
            return false;
        }
        return true;
    }

    private Optional<PublicKey> getPublicKey(Layout layout, String keyId) {
        return layout.getKeys().stream()
                .filter(publicKey -> publicKey.getKeyId().equals(keyId))
                .map(t -> {
					try {
						return t.getJavaPublicKey();
					} catch (GeneralSecurityException | IOException e) {
						log.error(e.getMessage());
						return null;
					}
				}).findFirst();
    }
}
