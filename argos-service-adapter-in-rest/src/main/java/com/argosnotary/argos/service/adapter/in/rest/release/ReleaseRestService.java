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
