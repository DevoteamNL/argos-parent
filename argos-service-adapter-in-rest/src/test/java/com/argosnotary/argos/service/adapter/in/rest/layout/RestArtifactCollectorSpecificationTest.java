/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
