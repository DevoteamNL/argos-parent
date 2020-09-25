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
package com.argosnotary.argos.service.adapter.in.rest.permission;

import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPermission;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRole;
import com.argosnotary.argos.service.domain.permission.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionRestServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper converter;

    private PermissionRestService service;

    @Mock
    private Role role;

    @Mock
    private RestRole restRole;

    @BeforeEach
    void setUp() {
        service = new PermissionRestService(roleRepository, converter);
    }

    @Test
    void getRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(converter.convertToRestRole(role)).thenReturn(restRole);
        ResponseEntity<List<RestRole>> response = service.getRoles();
        assertThat(response.getBody(), contains(restRole));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void getLocalPermissions() {
        ResponseEntity<List<RestPermission>> response = service.getPermissions();
        assertThat(response.getBody(), contains(RestPermission.values()));
        assertThat(response.getStatusCodeValue(), is(200));
    }
}