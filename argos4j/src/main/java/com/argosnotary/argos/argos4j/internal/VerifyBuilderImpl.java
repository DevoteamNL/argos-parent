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
package com.argosnotary.argos.argos4j.internal;

import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.ArtifactListBuilder;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.VerificationResult;
import com.argosnotary.argos.argos4j.VerifyBuilder;
import com.argosnotary.argos.domain.link.Artifact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class VerifyBuilderImpl implements VerifyBuilder {

    private final Argos4jSettings settings;

    private final ArtifactListBuilder artifactListBuilder;
    
    private final List<String> paths;

    @Override
    public VerifyBuilder addFileCollector(FileCollector collector) {
    	artifactListBuilder.addFileCollector(collector);
        return this;
    }

    @Override
    public VerificationResult verify() {
        List<Artifact> artifacts = artifactListBuilder.collect();

        log.info("verify artifacts {}", artifacts);
        return new ArgosServiceClient(settings).verify(artifacts.stream().map(Artifact::getHash).collect(Collectors.toList()), paths);
    }

}
