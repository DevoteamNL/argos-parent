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
package com.argosnotary.argos.argos4j.internal.mapper;


import com.argosnotary.argos.argos4j.rest.api.model.RestArtifact;
import com.argosnotary.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.argos4j.rest.api.model.RestReleaseResult;
import com.argosnotary.argos.argos4j.rest.api.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.release.ReleaseResult;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface RestMapper {

    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);
    
    ServiceAccountKeyPair convertFromRestServiceAccountKeyPair(RestServiceAccountKeyPair keyPair);

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);

    List<RestArtifact> convertToRestArtifacts(List<Artifact> artifacts);

    List<List<RestArtifact>> convertToRestArtifactsList(List<List<Artifact>> artifactsList);

    ReleaseResult convertToReleaseResult(RestReleaseResult releaseResult);
}
