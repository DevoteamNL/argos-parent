/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
package com.argosnotary.argos.service.adapter.in.rest.layout;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayout;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseConfiguration;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.layout.ReleaseConfigurationRepository;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.MODEL_CONSISTENCY;
import static com.argosnotary.argos.service.adapter.in.rest.layout.ValidationHelper.throwLayoutValidationException;
import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;
import static java.util.Collections.emptyList;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    private static final String SEGMENT_NAME = "segmentName";
    private final LayoutMetaBlockMapper layoutMetaBlockConverter;
    private final LayoutMetaBlockRepository layoutMetaBlockRepository;
    private final LayoutValidatorService validator;
    private final ApprovalConfigurationRepository approvalConfigurationRepository;
    private final ReleaseConfigurationRepository releaseConfigurationRepository;
    private final ConfigurationMapper configurationConverter;
    private final AccountSecurityContext accountSecurityContext;


    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<Void> validateLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, RestLayout restLayout) {
        Layout layout = layoutMetaBlockConverter.convertFromRestLayout(restLayout);
        validator.validateLayout(layout);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    @AuditLog
    @Transactional
    public ResponseEntity<RestLayoutMetaBlock> createOrUpdateLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) @AuditParam("supplyChainId") String supplyChainId, @AuditParam("layout") RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockConverter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        validator.validate(layoutMetaBlock);
        layoutMetaBlockRepository.createOrUpdate(layoutMetaBlock);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(layoutMetaBlockConverter.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestLayoutMetaBlock> getLayout(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        return layoutMetaBlockRepository.findBySupplyChainId(supplyChainId)
                .map(layoutMetaBlockConverter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    @Override
    @Transactional
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<List<RestApprovalConfiguration>> createApprovalConfigurations(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, 
            List<RestApprovalConfiguration> restApprovalConfigurations) {
        List<ApprovalConfiguration> approvalConfigurations = restApprovalConfigurations.stream()
                .map(restApprovalConfiguration -> convertAndValidate(supplyChainId, restApprovalConfiguration))
                .collect(Collectors.toList());
        approvalConfigurationRepository.saveAll(supplyChainId, approvalConfigurations);
        return ResponseEntity.ok(approvalConfigurations.stream()
                .map(configurationConverter::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));

    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalConfigurations(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        return ResponseEntity.ok(approvalConfigurationRepository
                .findBySupplyChainId(supplyChainId)
                .stream()
                .map(configurationConverter::convertToRestApprovalConfiguration)
                .collect(Collectors.toList()));
    }

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    public ResponseEntity<List<RestApprovalConfiguration>> getApprovalsForAccount(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {

        Account account = accountSecurityContext.getAuthenticatedAccount().orElseThrow(() -> new ArgosError("not logged in"));

        Optional<KeyPair> optionalKeyPair = Optional.ofNullable(account.getActiveKeyPair());
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockRepository.findBySupplyChainId(supplyChainId);

        if (optionalKeyPair.isPresent() && optionalLayoutMetaBlock.isPresent()) {
            String activeAccountKeyId = optionalKeyPair.get().getKeyId();
            Layout layout = optionalLayoutMetaBlock.get().getLayout();
            return ok(approvalConfigurationRepository.findBySupplyChainId(supplyChainId).stream().filter(approvalConf -> canApprove(approvalConf, activeAccountKeyId, layout)
            ).map(configurationConverter::convertToRestApprovalConfiguration).collect(Collectors.toList()));
        } else {
            return ok(emptyList());
        }
    }

    @Override
    @Transactional
    @PermissionCheck(permissions = Permission.LAYOUT_ADD)
    public ResponseEntity<RestReleaseConfiguration> createReleaseConfiguration(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, 
            RestReleaseConfiguration restReleaseConfiguration) {
        validateContextFieldsForCollectorSpecification(restReleaseConfiguration);
        ReleaseConfiguration releaseConfiguration = configurationConverter.convertFromRestReleaseConfiguration(restReleaseConfiguration);
        releaseConfiguration.setSupplyChainId(supplyChainId);
        releaseConfigurationRepository.save(releaseConfiguration);
        return ResponseEntity.ok(restReleaseConfiguration);
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<RestReleaseConfiguration> getReleaseConfiguration(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId) {
        return ResponseEntity.ok(releaseConfigurationRepository.findBySupplyChainId(supplyChainId)
                .map(configurationConverter::convertToRestReleaseConfiguration)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "release configuration not found")));
    }

    private boolean canApprove(ApprovalConfiguration approvalConf, String activeAccountKeyId, Layout layout) {
        return layout.getLayoutSegments().stream()
                .filter(layoutSegment -> layoutSegment.getName().equals(approvalConf.getSegmentName()))
                .map(LayoutSegment::getSteps).flatMap(Collection::stream)
                .filter(step -> step.getName().equals(approvalConf.getStepName()))
                .map(Step::getAuthorizedKeyIds).flatMap(Collection::stream)
                .anyMatch(authorizedKeyId -> authorizedKeyId.equals(activeAccountKeyId));
    }

    private void validateContextFieldsForCollectorSpecification(RestApprovalConfiguration approvalConfiguration) {
        approvalConfiguration.getArtifactCollectorSpecifications()
                .forEach(this::validateContextFieldsForCollectorSpecification);
    }

    private void validateContextFieldsForCollectorSpecification(RestReleaseConfiguration restReleaseConfiguration) {
        restReleaseConfiguration.getArtifactCollectorSpecifications()
                .forEach(this::validateContextFieldsForCollectorSpecification);
    }

    private void validateContextFieldsForCollectorSpecification(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        ContextInputValidator.of(restArtifactCollectorSpecification.getType()).validateContextFields(restArtifactCollectorSpecification);
    }

    private ApprovalConfiguration convertAndValidate(String supplyChainId, RestApprovalConfiguration restApprovalConfiguration) {
        validateContextFieldsForCollectorSpecification(restApprovalConfiguration);
        ApprovalConfiguration approvalConfiguration = configurationConverter.convertFromRestApprovalConfiguration(restApprovalConfiguration);
        approvalConfiguration.setSupplyChainId(supplyChainId);
        verifyStepNameAndSegmentNameExistInLayout(approvalConfiguration);
        return approvalConfiguration;
    }

    private void verifyStepNameAndSegmentNameExistInLayout(ApprovalConfiguration approvalConfiguration) {
        Map<String, Set<String>> segmentStepNameCombinations = getSegmentsAndSteps(approvalConfiguration);
        if (segmentNameIsNotPresentInLayout(approvalConfiguration, segmentStepNameCombinations)) {
            throwLayoutValidationException(
                    MODEL_CONSISTENCY,
                    SEGMENT_NAME,
                    "segment with name : " + approvalConfiguration.getSegmentName() + " does not exist in layout"
            );
        } else if (stepNameIsNotPresentInSegment(approvalConfiguration, segmentStepNameCombinations)) {
            throwLayoutValidationException(
                    MODEL_CONSISTENCY,
                    "stepName",
                    "step with name: " + approvalConfiguration.getStepName() + " in segment: " + approvalConfiguration.getSegmentName() + " does not exist in layout"
            );
        }
    }

    private Map<String, Set<String>> getSegmentsAndSteps(ApprovalConfiguration approvalConfiguration) {
        return layoutMetaBlockRepository.findBySupplyChainId(approvalConfiguration.getSupplyChainId())
                .map(layoutMetaBlock -> layoutMetaBlock
                        .getLayout().getLayoutSegments()
                        .stream()
                        .collect(Collectors
                                .toMap(LayoutSegment::getName,
                                        segment -> segment.getSteps()
                                                .stream().map(Step::getName)
                                                .collect(Collectors.toSet())))
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    private static boolean stepNameIsNotPresentInSegment(ApprovalConfiguration approvalConfiguration, Map<String, Set<String>> segmentStepNameCombinations) {
        return !segmentStepNameCombinations
                .get(approvalConfiguration.getSegmentName())
                .contains(approvalConfiguration.getStepName());
    }

    private static boolean segmentNameIsNotPresentInLayout(ApprovalConfiguration approvalConfiguration, Map<String, Set<String>> segmentStepNameCombinations) {
        return !segmentStepNameCombinations.containsKey(approvalConfiguration.getSegmentName());
    }


}
