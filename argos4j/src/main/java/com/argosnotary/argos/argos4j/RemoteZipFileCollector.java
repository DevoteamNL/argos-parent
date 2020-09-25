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
package com.argosnotary.argos.argos4j;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.net.URL;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@JsonDeserialize(builder = RemoteZipFileCollector.RemoteZipFileCollectorBuilder.class)
@EqualsAndHashCode(callSuper = true)
public class RemoteZipFileCollector extends RemoteCollector {

    @Builder
    public RemoteZipFileCollector(
            @JsonProperty("excludePatterns") @Nullable String excludePatterns, 
            @JsonProperty("normalizeLineEndings") @Nullable Boolean normalizeLineEndings,
            @JsonProperty("username") @Nullable String username, 
            @JsonProperty("password") @Nullable char[] password, 
            @JsonProperty("url") @NonNull URL url,
            @JsonProperty("configMap") @Nullable Map<String, String> parameterMap) {
        super(excludePatterns, normalizeLineEndings, username, password, url, parameterMap);
    }
}
