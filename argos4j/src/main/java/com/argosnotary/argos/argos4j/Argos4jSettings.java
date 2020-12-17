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
package com.argosnotary.argos.argos4j;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@JsonDeserialize(builder = Argos4jSettings.Argos4jSettingsBuilder.class)
@NoArgsConstructor
@AllArgsConstructor
public class Argos4jSettings implements Serializable {
    
    @JsonProperty("supplyChainName")
    private String supplyChainName;
    @JsonProperty("path")
    private List<String> path;
    @JsonProperty("keyId")
    private String keyId;
    @JsonProperty("keyPassphrase")
    private String keyPassphrase;
    @JsonProperty("argosServerBaseUrl")
    private String argosServerBaseUrl;
    @JsonProperty("releaseCollectors")
    private List<ReleaseCollector> releaseCollectors;
    
    public static Argos4jSettings readSettings(Path configFilePath) {
        Argos4jSettings argos4jSettings;
        try {
            String json = IOUtils.toString(configFilePath.toUri(), UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            argos4jSettings = objectMapper.readValue(json, Argos4jSettings.class );
        } catch (IOException e) {
                throw new Argos4jError(String.format("Error on reading config file %s: %s", configFilePath.toString(), e.getMessage()));
        }
        return argos4jSettings;
    }
    
    public void enrichReleaseCollectors(Map<String, Map<String, String>> configMaps) {
        if (releaseCollectors == null 
                || releaseCollectors.isEmpty() 
                || configMaps == null 
                || configMaps.isEmpty()) {
            return;
        }
        releaseCollectors.stream()
            .filter(c -> configMaps.containsKey(c.getName()))
            .forEach(c -> c.getCollector().enrich(configMaps.get(c.getName())));
    }

}
