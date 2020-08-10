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
package com.rabobank.argos.argos4j;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.nio.file.Path;
import java.util.Optional;

@Getter
@ToString
@JsonDeserialize(builder = LocalFileCollector.LocalFileCollectorBuilder.class)
@EqualsAndHashCode(callSuper = true)
public class LocalFileCollector extends FileCollector {

    private boolean followSymlinkDirs;

    /**
     * used to make all artifact uris relative from the base path
     */
    private Path basePath;

    /**
     * is the file or directory path
     */
    private Path path;

    @Builder
    public LocalFileCollector(
            @JsonProperty("excludePatterns") @Nullable String excludePatterns, 
            @JsonProperty("normalizeLineEndings") @Nullable Boolean normalizeLineEndings, 
            @JsonProperty("followSymlinkDirs") @Nullable Boolean followSymlinkDirs, 
            @JsonProperty("basePath") @Nullable Path basePath, 
            @JsonProperty("path") @NonNull Path path) {
        super(excludePatterns, normalizeLineEndings);
        this.followSymlinkDirs = Optional.ofNullable(followSymlinkDirs).orElse(true);
        this.basePath = basePath;
        this.path = path;
    }
}
