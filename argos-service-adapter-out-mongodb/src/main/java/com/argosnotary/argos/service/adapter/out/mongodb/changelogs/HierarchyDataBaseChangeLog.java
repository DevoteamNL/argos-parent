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
package com.argosnotary.argos.service.adapter.out.mongodb.changelogs;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

@ChangeLog
public class HierarchyDataBaseChangeLog {
    @ChangeSet(order = "001", id = "HierarchyChangelog-1", author = "michel")
    public void createHierarchyView(MongockTemplate template) throws IOException {
        String createTmpViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-hierarchy-tmp-view-01.json"), UTF_8);
        String createViewCommand = IOUtils.toString(getClass()
                .getResourceAsStream("/db-migration-scripts/create-hierarchy-view-01.json"), UTF_8);
        template.dropCollection("hierarchy_tmp");
        template.dropCollection("hierarchy");
        template.executeCommand(createTmpViewCommand);
        template.executeCommand(createViewCommand);

    }
}
