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
package com.argosnotary.argos.service.adapter.in.rest.supplychain;

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSupplyChain;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class RestSupplyChainTest {


    @Test
    void emptyRestSupplyChain() {
        assertThat(validate(new RestSupplyChain()), contains(expectedErrors(
                "name", "must not be null",
                "parentLabelId", "must not be null"
        )));
    }

    @Test
    void invalidRestSupplyChain() {
        assertThat(validate(new RestSupplyChain().name("invalid").parentLabelId("wrong")), contains(expectedErrors(
                "parentLabelId", "must match \"^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}?$\"",
                "parentLabelId", "size must be between 36 and 36"
        )));
    }

    @Test
    void validRestSupplyChain() {
        assertThat(validate(new RestSupplyChain().name("valid-1").parentLabelId(UUID.randomUUID().toString())), empty());
    }

}