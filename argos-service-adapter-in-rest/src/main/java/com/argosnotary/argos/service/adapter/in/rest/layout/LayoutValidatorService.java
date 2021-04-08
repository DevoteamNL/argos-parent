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
package com.argosnotary.argos.service.adapter.in.rest.layout;

import com.argosnotary.argos.domain.crypto.KeyIdProvider;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.service.adapter.in.rest.SignatureValidatorService;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.MODEL_CONSISTENCY;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class LayoutValidatorService {

    private final SupplyChainRepository supplyChainRepository;

    private final SignatureValidatorService signatureValidatorService;

    private final AccountService accountService;

    public void validate(LayoutMetaBlock layoutMetaBlock) {
        LayoutValidationReport report = new LayoutValidationReport();
        validateLayout(report, layoutMetaBlock.getLayout());
        validateSupplyChain(report, layoutMetaBlock);
        validateSignatures(report, layoutMetaBlock);
        if (!report.isValid()) {
            throwValidationException(report);
        }
    }

    public void validateLayout(Layout layout) {
        LayoutValidationReport report = new LayoutValidationReport();
        validateLayout(report, layout);
        if (!report.isValid()) {
            throwValidationException(report);
        }
    }

    private void validateLayout(LayoutValidationReport report, Layout layout) {
        validateStepNamesUnique(report, layout);
        validateMatchRuleDestinations(report, layout);
        validateAutorizationKeyIds(report, layout);
        validatePublicKeys(report, layout);
    }

    private void validatePublicKeys(LayoutValidationReport report, Layout layout) {
        layout.getKeys().forEach(key -> {
            if (!key.getKeyId().equals(KeyIdProvider.computeKeyId(key.getPublicKey()))) {
                report.addValidationMessage("keys",
                    "key with id " + key.getKeyId() + " does not match computed key id from public key");
            }
        });
    }

    private void validateStepNamesUnique(LayoutValidationReport report, Layout layout) {
        Set<String> stepNameSet = layout.getSteps().stream().map(Step::getName).collect(toSet());
        List<String> stepNameList = layout.getSteps().stream().map(Step::getName).collect(toList());
        if (stepNameSet.size() != stepNameList.size()) {
            report.addValidationMessage("steps",
                    "step names are not unique");
        }
    }

    private void validateMatchRuleDestinations(LayoutValidationReport report, Layout layout) {
        Set<String> steps = layout.getSteps().stream().map(Step::getName).collect(toSet());
        if (!layout.getExpectedEndProducts().stream().allMatch(matchRule -> steps.contains(matchRule.getDestinationStepName()))) {
            report.addValidationMessage("expectedEndProducts",
                    "expected product destination step name not found");
        }
    }

    private void validateSupplyChain(LayoutValidationReport report, LayoutMetaBlock layoutMetaBlock) {
        if (!supplyChainRepository.exists(layoutMetaBlock.getSupplyChainId())) {
            report.addValidationMessage("supplychain",
                    "supply chain not found : " + layoutMetaBlock.getSupplyChainId());
        }
    }

    private void validateSignatures(LayoutValidationReport report, LayoutMetaBlock layoutMetaBlock) {
        Set<String> uniqueKeyIds = layoutMetaBlock.getSignatures().stream().map(Signature::getKeyId).collect(toSet());
        if (layoutMetaBlock.getSignatures().size() != uniqueKeyIds.size()) {
            report.addValidationMessage("signatures",
                    "layout can't be signed more than one time with the same keyId");
        }

        layoutMetaBlock.getSignatures()
                .forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
    }

    private void validateAutorizationKeyIds(LayoutValidationReport report, Layout layout) {
        Set<String> publicKeyIds = layout.getKeys().stream().map(PublicKey::getKeyId).collect(toSet());
        layout.getAuthorizedKeyIds().forEach(keyid -> keyExists(report, keyid, publicKeyIds));
        layout.getSteps().forEach(step -> step.getAuthorizedKeyIds().forEach(keyid -> keyExists(report, keyid, publicKeyIds)));
    }

    private void keyExists(LayoutValidationReport report, String keyId, Set<String> publicKeyIds) {
        if(!publicKeyIds.contains(keyId)) { 
            report.addValidationMessage("key with",
                "keyId " + keyId + " not in the layout public keys");
        }
        if (!accountService.keyPairExists(keyId)) {
            report
                    .addValidationMessage("keys",
                            "keyId " + keyId + " not found");
        }
    }

    private void throwValidationException(LayoutValidationReport report) {
        throw LayoutValidationException
                .builder()
                .validationMessages(report.validationMessages)
                .build();
    }

    @Getter
    public static class LayoutValidationReport {
        private List<RestValidationMessage> validationMessages = new ArrayList<>();
        private void addValidationMessage(String field, String message) {
            validationMessages.add(new RestValidationMessage()
                    .type(MODEL_CONSISTENCY)
                    .field(field).message(message));
        }

        private boolean isValid() {
            return validationMessages.isEmpty();
        }
    }
}
