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
package com.argosnotary.argos.service.adapter.in.rest.release;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.ReleaseApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseArtifacts;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseResult;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.release.ReleaseService;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;

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
