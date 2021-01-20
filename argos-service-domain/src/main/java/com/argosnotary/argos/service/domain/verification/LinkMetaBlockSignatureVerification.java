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
