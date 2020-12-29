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
package com.argosnotary.argos.service.adapter.in.rest.account;

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.permission.LocalPermissions;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLocalPermissions;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPersonalAccount;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestProfile;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class PersonalAccountMapper {


    @Mapping(target = "id", source = "accountId")
    public abstract RestPersonalAccount convertToRestPersonalAccount(PersonalAccount personalAccount);

    @Mapping(target = "id", source = "accountId")
    public abstract RestProfile convertToRestProfile(PersonalAccount personalAccount);

    @Mapping(target = "id", source = "accountId")
    @Mapping(target = "roles", ignore = true)
    public abstract RestPersonalAccount convertToRestPersonalAccountWithoutRoles(PersonalAccount personalAccount);

    public abstract List<RestLocalPermissions> convertToRestLocalPermissionsList(Set<LocalPermissions> localPermissions);

    public abstract Set<LocalPermissions> convertToLocalPermissionsSet(List<RestLocalPermissions> localPermissions);

    public abstract RestLocalPermissions convertToRestLocalPermissions(LocalPermissions localPermissions);
    
    public abstract LocalPermissions convertToLocalPermissions(RestLocalPermissions localPermissions);
    
    public abstract Set<Role> convertToRoles(List<RestRole> roles);
    
    public abstract List<RestPermission> convertToRestPermissionList(Set<Permission> permissions);

    public abstract Set<Permission> convertToPermissionSet(List<RestPermission> permissions);
    
    public abstract List<RestRole> convertToRestRoleList(Set<Role> roles);
    
    public abstract RestRole convertToRestRole(Role role);

}
