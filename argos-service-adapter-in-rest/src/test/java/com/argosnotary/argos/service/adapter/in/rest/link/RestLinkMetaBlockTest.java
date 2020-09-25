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
package com.argosnotary.argos.service.adapter.in.rest.link;

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifact;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestHashAlgorithm;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestKeyAlgorithm;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLink;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSignature;
import org.junit.jupiter.api.Test;

import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class RestLinkMetaBlockTest {


    @Test
    void emptyRestLinkMetaBlock() {
        assertThat(validate(new RestLinkMetaBlock()), contains(expectedErrors(
                "link", "must not be null",
                "signature", "must not be null"
        )));
    }

    @Test
    void emptyRestLinkAndRestLinkMetaBlockAndRestSignature() {
        assertThat(validate(new RestLinkMetaBlock().link(new RestLink())
        		.signature(new RestSignature())), contains(expectedErrors(
                "link.layoutSegmentName", "must not be null",
                "link.runId", "must not be null",
                "link.stepName", "must not be null",
                "signature.hashAlgorithm", "must not be null",
                "signature.keyAlgorithm", "must not be null",
                "signature.keyId", "must not be null",
                "signature.signature", "must not be null")));
    }

    @Test
    void emptyArtifacts() {
        assertThat(validate(new RestLinkMetaBlock().link(new RestLink()
                .layoutSegmentName("segment Name")
                .runId("runId")
                .stepName("step Name")
                .addProductsItem(new RestArtifact())
                .addMaterialsItem(new RestArtifact()))
                .signature(new RestSignature()
                        .signature("signature")
                        .keyId("keyId")
                        .hashAlgorithm(RestHashAlgorithm.SHA384)
                        .keyAlgorithm(RestKeyAlgorithm.EC)
                )), contains(expectedErrors(
                "link.layoutSegmentName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                "link.materials[0].hash", "must not be null",
                "link.materials[0].uri", "must not be null",
                "link.products[0].hash", "must not be null",
                "link.products[0].uri", "must not be null",
                "link.stepName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                "signature.keyId", "must match \"^[0-9a-f]*$\"",
                "signature.keyId", "size must be between 64 and 64",
                "signature.signature", "must match \"^[0-9a-f]*$\"")));
    }

    @Test
    void invalidArtifacts() {
        assertThat(validate(new RestLinkMetaBlock().link(new RestLink()
                .layoutSegmentName("segmentName")
                .runId("runId")
                .stepName("stepName")
                .addProductsItem(new RestArtifact().hash("hash").uri("\t"))
                .addMaterialsItem(new RestArtifact().hash(" ").uri("\\\\")))
                .signature(createSignature()
                )), contains(expectedErrors(
                        "link.layoutSegmentName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                        "link.materials[0].hash", "must match \"^[0-9a-f]*$\"",
                        "link.materials[0].hash", "size must be between 64 and 64",
                        "link.materials[0].uri", "must match \"^(?!.*\\\\).*$\"",
                        "link.products[0].hash", "must match \"^[0-9a-f]*$\"",
                        "link.products[0].hash", "size must be between 64 and 64",
                        "link.stepName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"")));
    }

    @Test
    void validRestLinkMetaBlock() {
        assertThat(validate(new RestLinkMetaBlock().link(new RestLink()
                .layoutSegmentName("segment-name")
                .runId("runId")
                .stepName("step-name")
                .addProductsItem(new RestArtifact().hash("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254").uri("/test.jar"))
                .addMaterialsItem(new RestArtifact().hash("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254").uri("other.html")))
                .signature(createSignature()
                )), empty());
    }


    private RestSignature createSignature() {
        return new RestSignature()
        		.hashAlgorithm(RestHashAlgorithm.SHA256)
        		.keyAlgorithm(RestKeyAlgorithm.EC)
                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                .signature("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254");
    }
}