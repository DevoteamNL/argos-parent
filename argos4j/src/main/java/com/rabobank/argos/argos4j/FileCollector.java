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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, 
        include = JsonTypeInfo.As.PROPERTY, 
        property = "type")
      @JsonSubTypes({ 
        @Type(value = LocalFileCollector.class, name = "LocalFileCollector"), 
        @Type(value = LocalZipFileCollector.class, name = "LocalZipFileCollector"), 
        @Type(value = RemoteCollectorCollector.class, name = "RemoteCollectorCollector"), 
        @Type(value = RemoteFileCollector.class, name = "RemoteFileCollector"), 
        @Type(value = RemoteZipFileCollector.class, name = "RemoteZipFileCollector")
      })
@Getter
@EqualsAndHashCode
@ToString
public abstract class FileCollector implements Serializable {

    public static final String DEFAULT_EXCLUDE_PATTERNS = "{**.git/**,**.git\\**}";
    
    private static final String EXCLUDE_PATTERNS_FIELD = "excludePatterns";

    private String excludePatterns;

    private boolean normalizeLineEndings;

    
    public FileCollector(@Nullable String excludePatterns, @Nullable Boolean normalizeLineEndings) {
        this.excludePatterns = Optional.ofNullable(excludePatterns).orElse(DEFAULT_EXCLUDE_PATTERNS);
        this.normalizeLineEndings = Optional.ofNullable(normalizeLineEndings).orElse(false);
    }

    public void enrich(Map<String, String> configMap) {
        if (configMap.containsKey(EXCLUDE_PATTERNS_FIELD)) {
            this.excludePatterns = configMap.get(EXCLUDE_PATTERNS_FIELD);
            configMap.remove(EXCLUDE_PATTERNS_FIELD);
        }
    }
}
