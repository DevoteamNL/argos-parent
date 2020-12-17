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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argosnotary.argos.argos4j.LocalFileCollector;
import com.argosnotary.argos.argos4j.ReleaseCollector;

class ReleaseCollectorTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void builderTest() {
        ReleaseCollector collector = ReleaseCollector.builder()
                .collector(LocalFileCollector.builder()
                        .basePath(Paths.get("/foo"))
                        .path(Paths.get("bar"))
                        .excludePatterns("**").build()).name("name").build();
        assertTrue(collector.getCollector() instanceof LocalFileCollector);
    }
}
