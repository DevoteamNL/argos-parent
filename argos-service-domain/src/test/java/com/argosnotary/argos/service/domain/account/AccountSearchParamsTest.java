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
package com.argosnotary.argos.service.domain.account;

import org.junit.jupiter.api.Test;

import com.argosnotary.argos.domain.permission.Role;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AccountSearchParamsTest {
    private static final String LABEL_ID = "labelId";

    @Test
    void getRole() {
        assertThat(AccountSearchParams.builder().role(Role.ADMINISTRATOR).build().getRole(), is(Optional.of(Role.ADMINISTRATOR)));
    }

    @Test
    void getLocalPermissionsLabelId() {
        assertThat(AccountSearchParams.builder().localPermissionsLabelId(LABEL_ID).build().getLocalPermissionsLabelId(), is(Optional.of(LABEL_ID)));
    }
}