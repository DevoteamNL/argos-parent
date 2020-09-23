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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.SupplyChainHelper;
import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestAccountKeyInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class AccountKeyInfoMapper {
    @Mapping(target = "path", source = "pathToRoot", qualifiedByName = "convertToPath")
    @Mapping(target = "keyId", source = "key.keyId")
    @Mapping(target = "keyStatus", source = "key.status")
    public abstract RestAccountKeyInfo convertToRestAccountKeyInfo(AccountKeyInfo accountKeyInfo);

    @Named("convertToPath")
    protected String convertToPath(List<String> pathToRoot) {
        return String.join("/", SupplyChainHelper.reversePath(pathToRoot));
    }
}
