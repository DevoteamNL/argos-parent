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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class LocalFileCollectorTest {

    @Test
    void enrichTest() throws MalformedURLException {
        Map<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("applicationVersion", "version");
        paramMap.put("foo", "foo");
        paramMap.put("bar", "bar");

        paramMap.put("username", "username");
        Map<String,String> configMap = new HashMap<String,String>();
        configMap.putAll(paramMap);
        configMap.put("path", "otherPath");
        configMap.put("basePath", "otherBasePath");
        LocalFileCollector collector = LocalFileCollector.builder()
                .path(Paths.get("path"))
                    .build();
        collector.enrich(configMap);
        assertThat(collector.getBasePath(), is(Paths.get("otherBasePath")));
        assertThat(collector.getPath(), is(Paths.get("otherPath")));
        assertEquals(configMap, paramMap);
        
    }
    
    @Test
    void jsonTest() throws MalformedURLException, JsonMappingException, JsonProcessingException {
        String json = "{\"type\": \"LocalFileCollector\",\n" + 
                "\"path\": \"path\"}";

        LocalFileCollector expectedCollector = LocalFileCollector.builder()
                .path(Paths.get("path"))
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        LocalFileCollector collector = objectMapper.readValue(json, LocalFileCollector.class );
        assertEquals(expectedCollector, collector);
    }

}
