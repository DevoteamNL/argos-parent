/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
