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
package com.rabobank.argos.service.domain.release;

import com.rabobank.argos.domain.release.ReleaseDossier;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;

import java.util.List;
import java.util.Optional;

public interface ReleaseRepository {
    ReleaseDossierMetaData storeRelease(ReleaseDossierMetaData releaseDossierMetaData, ReleaseDossier releaseDossier);

    Optional<ReleaseDossierMetaData> findReleaseByReleasedArtifactsAndPath(List<String> releasedArtifacts, String path);

    Optional<String> getRawReleaseFileById(String id);
}
