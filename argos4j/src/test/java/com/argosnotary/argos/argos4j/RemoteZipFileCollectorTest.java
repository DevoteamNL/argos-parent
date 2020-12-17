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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class RemoteZipFileCollectorTest {

    @Test
    void enrichTest() throws MalformedURLException {
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("applicationVersion", "version");
        paramMap.put("foo", "foo");
        paramMap.put("bar", "bar");
        paramMap.put("artifactUri", "otherArtifactUri");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.putAll(paramMap);

        configMap.put("username", "username");
        configMap.put("password", "password");
        RemoteZipFileCollector collector = RemoteZipFileCollector.builder()
                .url(new URL("http://remote-collector"))
                    .build();
        collector.enrich(configMap);
        assertTrue(configMap.isEmpty());
        assertThat(collector.getUrl(), is(new URL("http://remote-collector")));
        assertEquals(paramMap, collector.getParameterMap());
        
    }
    
    @Test
    void jsonTest() throws MalformedURLException, JsonMappingException, JsonProcessingException {
        String json = "{\"type\": \"RemoteZipFileCollector\",\n" + 
                "\"url\": \"http://remote-collector\"}";

        RemoteZipFileCollector expectedCollector = RemoteZipFileCollector.builder()
                    .url(new URL("http://remote-collector"))
                    .build();
        ObjectMapper objectMapper = new ObjectMapper();
        RemoteZipFileCollector collector = objectMapper.readValue(json, RemoteZipFileCollector.class );
        assertEquals(expectedCollector, collector);
    }
}
