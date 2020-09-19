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
package com.rabobank.argos.service.adapter.in.rest.release;

import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.permission.Permission;
import com.rabobank.argos.domain.release.ReleaseResult;
import com.rabobank.argos.service.adapter.in.rest.api.handler.ReleaseApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestReleaseArtifacts;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestReleaseResult;
import com.rabobank.argos.service.domain.auditlog.AuditLog;
import com.rabobank.argos.service.domain.auditlog.AuditParam;
import com.rabobank.argos.service.domain.release.ReleaseService;
import com.rabobank.argos.service.domain.security.LabelIdCheckParam;
import com.rabobank.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.rabobank.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReleaseRestService implements ReleaseApi {

    private final ReleaseService releaseService;
    private final ReleaseArtifactMapper artifactMapper;
    private final ReleaseResultMapper releaseResultMapper;

    @Override
    @PermissionCheck(permissions = Permission.RELEASE)
    @AuditLog
    public ResponseEntity<RestReleaseResult> createRelease(
            @LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
            @AuditParam("supplyChainId") String supplyChainId,
            @AuditParam("releaseArtifacts") RestReleaseArtifacts restReleaseArtifacts) {
        List<Set<Artifact>> artifacts = artifactMapper.mapToArtifacts(restReleaseArtifacts.getReleaseArtifacts());
        ReleaseResult releaseResult = releaseService.createRelease(supplyChainId, artifacts);
        return ResponseEntity.ok(releaseResultMapper.maptoRestReleaseResult(releaseResult));
    }
}
