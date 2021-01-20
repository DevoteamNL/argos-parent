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

import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.layout.rule.MatchRule;
import com.argosnotary.argos.service.adapter.in.rest.SignatureValidatorService;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.MODEL_CONSISTENCY;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutValidatorServiceTest {

    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private SupplyChainRepository supplyChainRepository;

    @Mock
    private SignatureValidatorService signatureValidatorService;

    @Mock
    private AccountService accountService;

    private LayoutValidatorService service;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Signature signature;

    @Mock
    private Layout layout;

    @Mock
    private Step step;

    @Mock
    private LayoutSegment layoutSegment;

    @Mock
    private LayoutSegment layoutSegment2;

    @Mock
    private MatchRule matchRule;

    @Mock
    private MatchRule matchRule2;

    private PublicKey publicKey1;

    private PublicKey publicKey2;


    @BeforeEach
    void setUp() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, OperatorCreationException, PemGenerationException {
        publicKey1 = KeyPair.createKeyPair("test".toCharArray());
        publicKey2 = KeyPair.createKeyPair("test".toCharArray());

        service = new LayoutValidatorService(supplyChainRepository, signatureValidatorService, accountService);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
    }

    @Test
    void validateAllOkay() {
        mockPublicKeys();

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(layoutMetaBlock.getSignatures()).thenReturn(singletonList(signature));

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(layoutSegment.getName()).thenReturn("segmentName");
        when(step.getName()).thenReturn("stepName");
        when(layout.getExpectedEndProducts()).thenReturn(singletonList(matchRule));
        when(matchRule.getDestinationSegmentName()).thenReturn("segmentName");
        when(matchRule.getDestinationStepName()).thenReturn("stepName");
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey2.getKeyId()));

        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(true);
        when(accountService.keyPairExists(publicKey2.getKeyId())).thenReturn(true);

        service.validate(layoutMetaBlock);
        verify(signatureValidatorService).validateSignature(layout, signature);
    }

    private void mockPublicKeys() {
        when(layout.getKeys()).thenReturn(List.of(publicKey1, publicKey2));
    }

    @Test
    void validateDuplicateKeyId() {

        mockPublicKeys();
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);
        when(signature.getKeyId()).thenReturn(publicKey1.getKeyId());
        when(layoutMetaBlock.getSignatures()).thenReturn(Arrays.asList(signature, signature));

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey2.getKeyId()));

        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(true);
        when(accountService.keyPairExists(publicKey2.getKeyId())).thenReturn(true);

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });

        assertThat(layoutValidationException.getValidationMessages(), hasSize(1));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("signatures"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("layout can't be signed more than one time with the same keyId"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));
    }

    @Test
    void validateMissingPublicKey() {

        when(layout.getKeys()).thenReturn(List.of(publicKey1));

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey2.getKeyId()));

        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(true);
        when(accountService.keyPairExists(publicKey2.getKeyId())).thenReturn(true);

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });

        assertThat(layoutValidationException.getValidationMessages(), hasSize(1));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("authorizedKeyIds"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("The defined Public keys are not equal to all defined Authorized keys"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateOtherPublicKey() {

        when(layout.getKeys()).thenReturn(List.of(publicKey1));

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey2.getKeyId()));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey2.getKeyId()));
        when(accountService.keyPairExists(publicKey2.getKeyId())).thenReturn(true);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });

        assertThat(layoutValidationException.getValidationMessages(), hasSize(1));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("authorizedKeyIds"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("The defined Public keys are not equal to all defined Authorized keys"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateInvalidPublicKeyId() {

        when(layout.getKeys()).thenReturn(List.of(publicKey1));

        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);

        publicKey1.setKeyId("otherKeyId");
        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(true);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(layoutValidationException.getValidationMessages(), hasSize(1));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("keys"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is(String.format("key with id %s does not match computed key id from public key", publicKey1.getKeyId())));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateKey2NotFound() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(step.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey2.getKeyId()));

        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(true);
        when(accountService.keyPairExists(publicKey2.getKeyId())).thenReturn(false);

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("keys"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is(String.format("keyId %s not found", publicKey2.getKeyId())));

    }

    @Test
    void validateKey1NotFound() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(supplyChainRepository.exists(SUPPLY_CHAIN_ID)).thenReturn(true);

        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(true);

        when(layout.getAuthorizedKeyIds()).thenReturn(singletonList(publicKey1.getKeyId()));
        when(accountService.keyPairExists(publicKey1.getKeyId())).thenReturn(false);

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });

        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("keys"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is(String.format(String.format("keyId %s not found", publicKey1.getKeyId()))));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateNoSupplyChain() {
        when(layoutMetaBlock.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });

        assertThat(layoutValidationException.getValidationMessages(), hasSize(1));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("supplychain"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("supply chain not found : " + SUPPLY_CHAIN_ID));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateSegmentNamesNotUnique() {
        when(layoutSegment.getName()).thenReturn("segment 1");
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment, layoutSegment));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });

        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("layoutSegments"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("segment names are not unique"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateStepNamesNotUnique() {
        when(layoutSegment.getName()).thenReturn("segment 1");
        when(step.getName()).thenReturn("stepName");
        when(layoutSegment.getSteps()).thenReturn(List.of(step, step));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("layoutSegments"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is(String.format("step names for segment: %s are not unique", layoutSegment.getName())));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));
    }

    @Test
    void validateDestinationStepNameNotFound() {
        when(layout.getExpectedEndProducts()).thenReturn(singletonList(matchRule));
        when(matchRule.getDestinationSegmentName()).thenReturn("segmentName");
        when(matchRule.getDestinationStepName()).thenReturn("otherStepName");
        when(layoutSegment.getName()).thenReturn("segmentName");
        when(step.getName()).thenReturn("stepName");
        when(layoutSegment.getSteps()).thenReturn(List.of(step));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("expectedEndProducts"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("expected product destination step name not found"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateDestinationSegmentNameNotFound() {
        when(layout.getExpectedEndProducts()).thenReturn(singletonList(matchRule));
        when(matchRule.getDestinationSegmentName()).thenReturn("otherSegmentName");
        when(layoutSegment.getName()).thenReturn("segmentName");
        when(step.getName()).thenReturn("stepName");
        when(layoutSegment.getSteps()).thenReturn(List.of(step));
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("expectedEndProducts"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("expected product destination step name not found"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));

    }

    @Test
    void validateExpectedProductsHaveSameSegmentName() {
        when(layoutSegment.getSteps()).thenReturn(singletonList(step));
        when(layoutSegment.getName()).thenReturn("segmentName");
        when(layoutSegment2.getSteps()).thenReturn(singletonList(step));
        when(layoutSegment2.getName()).thenReturn("othersegmentName");
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment, layoutSegment2));
        when(step.getName()).thenReturn("stepName");
        when(layout.getExpectedEndProducts()).thenReturn(List.of(matchRule, matchRule2));
        when(matchRule.getDestinationSegmentName()).thenReturn("segmentName");
        when(matchRule.getDestinationStepName()).thenReturn("stepName");
        when(matchRule2.getDestinationSegmentName()).thenReturn("othersegmentName");
        when(matchRule2.getDestinationStepName()).thenReturn("stepName");

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> {
            service.validate(layoutMetaBlock);
        });
        assertThat(layoutValidationException.getValidationMessages(), hasSize(2));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("expectedEndProducts"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("segment names for expectedProducts should all be the same"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getType(), is(MODEL_CONSISTENCY));
    }
}
