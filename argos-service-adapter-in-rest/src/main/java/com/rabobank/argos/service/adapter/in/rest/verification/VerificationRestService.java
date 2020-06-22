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
package com.rabobank.argos.service.adapter.in.rest.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.service.adapter.in.rest.api.handler.VerificationApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import com.rabobank.argos.service.domain.auditlog.AuditLog;
import com.rabobank.argos.service.domain.auditlog.AuditParam;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import com.rabobank.argos.service.domain.verification.VerificationProvider;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

import static com.rabobank.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class VerificationRestService implements VerificationApi {

    private final VerificationProvider verificationProvider;

    private final LayoutMetaBlockRepository repository;

    private final ArtifactMapper artifactMapper;

    private final VerificationResultMapper verificationResultMapper;

    @Override
    @PermissionCheck(permissions = Permission.VERIFY)
    @AuditLog
    public ResponseEntity<RestVerificationResult> performVerification(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
                                                                      @AuditParam("supplyChainId") String supplyChainId,
                                                                      @AuditParam("verifyCommand") @Valid RestVerifyCommand restVerifyCommand) {

        LayoutMetaBlock layoutMetaBlock = repository.findBySupplyChainId(supplyChainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "no active layout could be found for supplychain:" + supplyChainId));

        List<Artifact> expectedProducts = artifactMapper.mapToArtifacts(restVerifyCommand.getExpectedProducts());
        VerificationRunResult verificationRunResult = verificationProvider.verifyRun(layoutMetaBlock, expectedProducts);
        return ResponseEntity.ok(verificationResultMapper.mapToRestVerificationResult(verificationRunResult));
    }
}
