/*
 * Copyright (C) 2020 Argos Notary
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

import com.argosnotary.argos.argos4j.ArtifactListBuilder;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.domain.link.Artifact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArtifactListBuilderImpl implements ArtifactListBuilder {

	private final List<FileCollector> fileCollectors = new ArrayList<>();

	@Override
	public void addFileCollector(FileCollector collector) {
		fileCollectors.add(collector);
	}

	@Override
	public List<Artifact> collect() {
		return fileCollectors.stream().map(ArtifactCollectorFactory::build).map(ArtifactCollector::collect).flatMap(List::stream).collect(Collectors.toList());
	}

	@Override
	public List<List<Artifact>> collectAsArtifactLists() {
		return fileCollectors.stream().map(ArtifactCollectorFactory::build).map(ArtifactCollector::collect).collect(Collectors.toList());
	}

}
