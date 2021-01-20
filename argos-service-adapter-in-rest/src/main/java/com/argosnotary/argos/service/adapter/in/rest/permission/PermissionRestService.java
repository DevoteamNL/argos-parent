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
package com.argosnotary.argos.service.adapter.in.rest.permission;

import com.argosnotary.argos.service.adapter.in.rest.api.handler.PermissionsApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRole;
import com.argosnotary.argos.service.domain.permission.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PermissionRestService implements PermissionsApi {

    @Override
    public ResponseEntity<List<RestRole>> getRoles() {
        return ResponseEntity.ok(List.of(RestRole.values()));
    }

    @Override
    public ResponseEntity<List<RestPermission>> getPermissions() {
        return ResponseEntity.ok(List.of(RestPermission.values()));
    }
}
