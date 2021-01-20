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

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.validate;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class RestApprovalConfigurationTest {
    @Test
    void emptyRestLayoutMetaBlock() {
        assertThat(validate(new RestApprovalConfiguration()), contains(expectedErrors(
                "artifactCollectorSpecifications", "size must be between 1 and 20",
                "segmentName", "must not be null",
                "stepName", "must not be null")));
    }

    @Test
    void incorrectStepName() throws URISyntaxException {
        assertThat(validate(new RestApprovalConfiguration()
                .stepName("name%")
                .segmentName("segment").
                        artifactCollectorSpecifications(singletonList(new RestArtifactCollectorSpecification()
                                .name("xldeploy").type(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY)
                                .uri(new URI("http://xldeploy.nl"))))
        ), contains(expectedErrors(
                "stepName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"")));

    }

    @Test
    void incorrectSegmentName() throws URISyntaxException {
        assertThat(validate(new RestApprovalConfiguration().stepName("name").segmentName("&segment")
                .artifactCollectorSpecifications(singletonList(new RestArtifactCollectorSpecification()
                        .name("xldeploy")
                        .type(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY)
                        .uri(new URI("http://xldeploy.nl"))))
        ), contains(expectedErrors(
                "segmentName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"")));

    }
}
