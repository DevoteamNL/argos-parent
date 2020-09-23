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
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString(callSuper = true)
@JsonDeserialize(builder = RemoteCollectorCollector.RemoteCollectorCollectorBuilder.class)
@EqualsAndHashCode(callSuper = true)
public class RemoteCollectorCollector extends RemoteCollector {
    
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    
    @Builder
    public RemoteCollectorCollector(
            @JsonProperty("excludePatterns") @Nullable String excludePatterns, 
            @JsonProperty("normalizeLineEndings") @Nullable Boolean normalizeLineEndings,
            @JsonProperty("username") @Nullable String username, 
            @JsonProperty("password") @Nullable char[] password, 
            @JsonProperty("url") @NonNull URL url, 
            @JsonProperty("parameterMap") @Nullable Map<String, String> parameterMap) {
        super(excludePatterns, normalizeLineEndings, username, password, url, parameterMap);
    }
    
    @Override
    public void enrich(Map<String, String> configMap) {
        if (this.getParameterMap() == null) {
            this.setParameterMap(new HashMap<>());
        }
        if (configMap.containsKey(USERNAME_FIELD)) {
            this.getParameterMap().put(USERNAME_FIELD, configMap.get(USERNAME_FIELD));
            configMap.remove(USERNAME_FIELD);
        }
        if (configMap.containsKey(PASSWORD_FIELD)) {
            this.getParameterMap().put(PASSWORD_FIELD, configMap.get(PASSWORD_FIELD));
            configMap.remove(PASSWORD_FIELD);
        }
        super.enrich(configMap);
    }
}
