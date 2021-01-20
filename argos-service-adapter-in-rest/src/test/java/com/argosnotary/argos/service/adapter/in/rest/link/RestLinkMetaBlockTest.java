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