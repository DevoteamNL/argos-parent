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
package com.argosnotary.argos.service.adapter.in.rest.layout;

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class RestArtifactCollectorSpecificationTest {

    @Test
    void incorrectName() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollectorSpecification()
                .name("Name$")
                .uri(new URI("http://uri.com"))
                .type(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY)
        ), contains(expectedErrors(
                "name", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"")));

    }


    @Test
    void emptyName() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollectorSpecification()
                .uri(new URI("http://uri.com"))
                .type(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY)
        ), contains(expectedErrors(
                "name", "must not be null")));
    }

    @Test
    void emptyUri() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollectorSpecification()
                .name("name")
                .type(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY)
        ), contains(expectedErrors(
                "uri", "must not be null")));
    }

    @Test
    void emptyType() throws URISyntaxException {
        assertThat(validate(new RestArtifactCollectorSpecification()
                .name("name")
                .uri(new URI("http://uri.com"))
        ), contains(expectedErrors(
                "type", "must not be null")));
    }
}
