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

import com.argosnotary.argos.domain.crypto.signing.SignatureValidator;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Optional;

import static com.argosnotary.argos.service.domain.verification.Verification.Priority.LINK_METABLOCK_SIGNATURE;
import static java.util.stream.Collectors.toList;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class LinkMetaBlockSignatureVerification implements Verification {

    @Override
    public Priority getPriority() {
        return LINK_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        context.removeLinkMetaBlocks(context.getLinkMetaBlocks().stream()
                .filter(linkMetaBlock -> !okay(context.getLayoutMetaBlock(), linkMetaBlock)).collect(toList()));
        return VerificationRunResult.okay();
    }

    private boolean okay(LayoutMetaBlock layoutMetaBlock, LinkMetaBlock linkMetaBlock) {
        return getPublicKey(layoutMetaBlock, linkMetaBlock.getSignature().getKeyId())
                .map(keyPair -> SignatureValidator.isValid(linkMetaBlock.getLink(),
                        linkMetaBlock.getSignature(), keyPair))
                .orElse(false);
    }

    private Optional<PublicKey> getPublicKey(LayoutMetaBlock layoutMetaBlock, String keyId) {
        Optional<com.argosnotary.argos.domain.crypto.PublicKey> keyOptional = getKeyById(layoutMetaBlock, keyId);
        if (keyOptional.isEmpty()) {
            log.error("key with id: {} not found in layout", keyId);
        }
        return keyOptional.map(t -> {
			try {
				return t.getJavaPublicKey();
			} catch (GeneralSecurityException | IOException e) {
				log.error(e.getMessage());
				return null;
			}
		});
    }

    private Optional<com.argosnotary.argos.domain.crypto.PublicKey> getKeyById(LayoutMetaBlock layoutMetaBlock, String keyId) {
        return layoutMetaBlock.getLayout().getKeys().stream().filter(publicKey -> publicKey.getKeyId().equals(keyId)).findFirst();
    }

}
