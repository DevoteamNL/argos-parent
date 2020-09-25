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
package com.argosnotary.argos.integrationtest.service.link;


import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestLinkMetaBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LinkMetaBlockMapper {

    @Mapping(target = "supplyChainId", ignore = true)
    LinkMetaBlock convertFromRestLinkMetaBlock(RestLinkMetaBlock metaBlock);

    RestLinkMetaBlock convertToRestLinkMetaBlock(LinkMetaBlock metaBlock);
}
