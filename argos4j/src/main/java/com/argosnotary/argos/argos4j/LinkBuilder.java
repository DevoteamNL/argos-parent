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

import java.io.Serializable;
import java.util.List;

import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.LinkMetaBlock;

public interface LinkBuilder extends Serializable {
    Argos4jSettings getSettings();
    
    void addMaterials(List<Artifact> artifacts);
    
    void addProducts(List<Artifact> artifacts);

    void collectMaterials(FileCollector collector);

    void collectProducts(FileCollector collector);

    LinkMetaBlock create(char[] signingKeyPassphrase);

    void store(char[] signingKeyPassphrase);
}
