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
