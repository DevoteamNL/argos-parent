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

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class PermissionRestServiceTest {

    private PermissionRestService service;

    @BeforeEach
    void setUp() {
        service = new PermissionRestService();
    }

    @Test
    void getRoles() {
        ResponseEntity<List<RestRole>> response = service.getRoles();
        assertThat(response.getBody(), contains(RestRole.values()));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void getLocalPermissions() {
        ResponseEntity<List<RestPermission>> response = service.getPermissions();
        assertThat(response.getBody(), contains(RestPermission.values()));
        assertThat(response.getStatusCodeValue(), is(200));
    }
}