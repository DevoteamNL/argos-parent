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