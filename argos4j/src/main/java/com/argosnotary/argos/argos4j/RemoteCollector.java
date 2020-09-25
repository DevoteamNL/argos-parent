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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class RemoteCollector extends FileCollector {
    
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private static final String URL_FIELD = "url";

    /**
     * optional for basic authentication
     */
    private String username;

    private char[] password;
    
    private Map<String, String> parameterMap;

    /**
     * the url of the remote file
     */
    private URL url;

    public RemoteCollector(
            @Nullable String excludePatterns, 
            @Nullable Boolean normalizeLineEndings, 
            @Nullable String username, 
            char[] password, 
            @NonNull URL url,
            @Nullable Map<String, String> parameterMap) {
        super(excludePatterns, normalizeLineEndings);
        this.username = username;
        this.password = password;
        this.url = url;
        this.parameterMap = parameterMap;
    }

    @Override
    public void enrich(Map<String, String> configMap) {
        super.enrich(configMap);
        if (configMap.containsKey(USERNAME_FIELD)) {
            this.username = configMap.get(USERNAME_FIELD);
            configMap.remove(USERNAME_FIELD);
        }
        if (configMap.containsKey(PASSWORD_FIELD)) {
            this.password = configMap.get(PASSWORD_FIELD).toCharArray();
            configMap.remove(PASSWORD_FIELD);
        }
        if (configMap.containsKey(URL_FIELD)) {
            try {
                this.url = new URL(configMap.get(URL_FIELD));
            } catch (MalformedURLException e) {
                throw new Argos4jError(String.format("Error in url [%s]: [%s]", configMap.get(URL_FIELD), e.getMessage()));
            }
            configMap.remove(URL_FIELD);
        }
        if (parameterMap == null) {
            parameterMap = new HashMap<>();
        }
        parameterMap.putAll(configMap);
        configMap.clear();
    }
}
