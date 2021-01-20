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
package com.argosnotary.argos.service.adapter.in.rest.verification;

import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.VerificationApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestVerificationResult;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.release.ReleaseRepository;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import com.argosnotary.argos.service.domain.verification.VerificationProvider;
import com.argosnotary.argos.service.domain.verification.VerificationRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class VerificationRestService implements VerificationApi {

    private final VerificationProvider verificationProvider;

    private final LayoutMetaBlockRepository repository;

    private final ReleaseRepository releaseRepository;

    private final ArtifactMapper artifactMapper;

    private final VerificationResultMapper verificationResultMapper;

    @Override
    public ResponseEntity<RestVerificationResult> getVerification(List<String> artifactHashes, List<String> paths) {
        log.info("Verification request for paths [{}] and hashes [{}].", paths, artifactHashes);
        boolean isvalid = releaseRepository.artifactsAreReleased(artifactHashes, paths);
        log.info("Verify result [{}] for paths [{}] and hashes [{}].", isvalid, paths, artifactHashes);
        return ResponseEntity.ok(new RestVerificationResult().runIsValid(isvalid));
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    @AuditLog
    public ResponseEntity<RestVerificationResult> performVerification(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
                                                                      @AuditParam("supplyChainId") String supplyChainId,
                                                                      @AuditParam("verifyCommand") RestVerifyCommand restVerifyCommand) {

        LayoutMetaBlock layoutMetaBlock = repository.findBySupplyChainId(supplyChainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "no active layout could be found for supplychain:" + supplyChainId));

        List<Artifact> expectedProducts = artifactMapper.mapToArtifacts(restVerifyCommand.getExpectedProducts());
        VerificationRunResult verificationRunResult = verificationProvider.verifyRun(layoutMetaBlock, expectedProducts);
        return ResponseEntity.ok(verificationResultMapper.mapToRestVerificationResult(verificationRunResult));
    }
}
