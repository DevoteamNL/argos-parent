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
package com.argosnotary.argos.argos4j.internal;

import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.LocalZipFileCollector;
import com.argosnotary.argos.domain.link.Artifact;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ZipArtifactCollector implements ArtifactCollector {


    private final ZipStreamArtifactCollector zipStreamArtifactCollector;
    private final Path zipPath;

    public ZipArtifactCollector(LocalZipFileCollector fileCollector) {
        zipPath = fileCollector.getPath();
        zipStreamArtifactCollector = new ZipStreamArtifactCollector(fileCollector);
    }

    @Override
    public List<Artifact> collect() {
        try (FileInputStream fis = new FileInputStream(zipPath.toFile())) {
            return zipStreamArtifactCollector.collect(fis);
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}
