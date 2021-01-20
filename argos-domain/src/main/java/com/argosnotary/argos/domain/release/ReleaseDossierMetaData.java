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
