/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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
package com.argosnotary.argos.domain.release;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.String.join;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Builder
@Getter
@Setter
@EqualsAndHashCode
public class ReleaseDossierMetaData {
    private String documentId;
    private OffsetDateTime releaseDate;
    private String supplyChainPath;
    private List<List<String>> releaseArtifacts;

    public static String createHashFromArtifactList(List<String> artifactList) {
        ArrayList<String> list = new ArrayList<>(artifactList);
        Collections.sort(list);
        return sha256Hex(join("", list));
    }
}
