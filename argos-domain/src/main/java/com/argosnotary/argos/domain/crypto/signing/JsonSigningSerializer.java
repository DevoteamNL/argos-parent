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
package com.argosnotary.argos.domain.crypto.signing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import org.mapstruct.factory.Mappers;

import static java.util.Comparator.comparing;

public class JsonSigningSerializer implements SigningSerializer {

    @Override
    public String serialize(Link link) {
        Link linkClone = Mappers.getMapper(Cloner.class).clone(link);
        linkClone.getMaterials().sort(comparing(Artifact::getUri));
        linkClone.getProducts().sort(comparing(Artifact::getUri));
        return serializeSignable(linkClone);
    }

    @Override
    public String serialize(Layout layout) {
        Layout layoutClone = Mappers.getMapper(Cloner.class).clone(layout);
        layoutClone.getLayoutSegments().sort(comparing(LayoutSegment::getName));
        layoutClone.getLayoutSegments().forEach(layoutSegment -> layoutSegment.getSteps().sort(comparing(Step::getName)));
        return serializeSignable(layoutClone);
    }

    private String serializeSignable(Object signable) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        try {
            return objectMapper.writeValueAsString(signable);
        } catch (JsonProcessingException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

}
