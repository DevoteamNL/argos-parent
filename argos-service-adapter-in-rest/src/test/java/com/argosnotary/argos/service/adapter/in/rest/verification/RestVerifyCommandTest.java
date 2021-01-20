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
package com.argosnotary.argos.service.adapter.in.rest.verification;

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import org.junit.jupiter.api.Test;

import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class RestVerifyCommandTest {


    @Test
    void emptyRestVerifyCommand() {
        assertThat(validate(new RestVerifyCommand()), contains(expectedErrors(
                "expectedProducts", "size must be between 1 and 4096"
        )));
    }

    @Test
    void emptyRestArtifact() {
        assertThat(validate(new RestVerifyCommand().addExpectedProductsItem(new RestArtifact())), contains(expectedErrors(
                "expectedProducts[0].hash", "must not be null",
                "expectedProducts[0].uri", "must not be null"
        )));
    }

    @Test
    void invalidRestArtifact() {
        assertThat(validate(new RestVerifyCommand().addExpectedProductsItem(new RestArtifact().hash("hash").uri("\t\t\t\\wrong"))), contains(expectedErrors(
                "expectedProducts[0].hash", "must match \"^[0-9a-f]*$\"",
                "expectedProducts[0].hash", "size must be between 64 and 64",
                "expectedProducts[0].uri", "must match \"^(?!.*\\\\).*$\""
        )));
    }

    @Test
    void validRestArtifact() {
        assertThat(validate(new RestVerifyCommand().addExpectedProductsItem(new RestArtifact().hash("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254").uri("/test.jar"))), empty());
    }

}