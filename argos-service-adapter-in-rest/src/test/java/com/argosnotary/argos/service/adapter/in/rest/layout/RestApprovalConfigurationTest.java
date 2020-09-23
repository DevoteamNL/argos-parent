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
