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
package com.argosnotary.argos.service.adapter.out.mongodb.changelogs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.index.IndexOperations;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

import static com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl.COLLECTION_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseDatabaseChangelogTest {

    @Mock
    private MongockTemplate mongoTemplate;

    @Mock
    private IndexOperations indexOperations;

    private ReleaseDatabaseChangelog releaseDatabaseChangelog;

    @BeforeEach
    void setUp() {
        releaseDatabaseChangelog = new ReleaseDatabaseChangelog();
    }

    @Test
    void addIndex() {
        when(mongoTemplate.indexOps(COLLECTION_NAME)).thenReturn(indexOperations);
        releaseDatabaseChangelog.addIndex(mongoTemplate);
        verify(indexOperations, times(1)).ensureIndex(any());
    }
}