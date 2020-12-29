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