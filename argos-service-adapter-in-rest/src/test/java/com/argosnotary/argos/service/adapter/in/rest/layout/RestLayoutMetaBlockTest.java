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

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestHashAlgorithm;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestKeyAlgorithm;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayout;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayoutSegment;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestMatchRule;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestPublicKey;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestRule;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSignature;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestStep;
import org.junit.jupiter.api.Test;

import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.expectedErrors;
import static com.argosnotary.argos.service.adapter.in.rest.ValidateHelper.validate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

class RestLayoutMetaBlockTest {


    @Test
    void emptyRestLayoutMetaBlock() {
        assertThat(validate(new RestLayoutMetaBlock()), contains(expectedErrors(
                "layout", "must not be null",
                "signatures", "size must be between 1 and 20")));
    }

    @Test
    void emptyRestLayout() {
        assertThat(validate(new RestLayoutMetaBlock()
                .addSignaturesItem(new RestSignature())
                .layout(new RestLayout())), contains(expectedErrors(
                "layout.authorizedKeyIds", "size must be between 1 and 256",
                "layout.expectedEndProducts", "size must be between 1 and 4096",
                "layout.keys", "size must be between 1 and 256",
                "layout.layoutSegments", "size must be between 1 and 256",
                "signatures[0].hashAlgorithm", "must not be null",
                "signatures[0].keyAlgorithm", "must not be null",
                "signatures[0].keyId", "must not be null",
                "signatures[0].signature", "must not be null")));
    }

    @Test
    void emptySubItemsRestLayout() {
        assertThat(validate(new RestLayoutMetaBlock()
                .addSignaturesItem(new RestSignature()
                        .keyId("keyId")
                        .signature("signature")
                        .hashAlgorithm(RestHashAlgorithm.SHA256)
                        .keyAlgorithm(RestKeyAlgorithm.EC))
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule())
                        .addKeysItem(new RestPublicKey()).addLayoutSegmentsItem(new RestLayoutSegment()))), contains(expectedErrors(
                "layout.expectedEndProducts[0].destinationSegmentName", "must not be null",
                "layout.expectedEndProducts[0].destinationStepName", "must not be null",
                "layout.expectedEndProducts[0].destinationType", "must not be null",
                "layout.expectedEndProducts[0].pattern", "must not be null",
                "layout.keys[0].keyId", "must not be null",
                "layout.keys[0].publicKey", "must not be null",
                "layout.layoutSegments[0].name", "must not be null",
                "layout.layoutSegments[0].steps", "size must be between 1 and 256",
                "signatures[0].keyId", "must match \"^[0-9a-f]*$\"",
                "signatures[0].keyId", "size must be between 64 and 64",
                "signatures[0].signature", "must match \"^[0-9a-f]*$\""
                )));
    }

    @Test
    void subItemsRestLayout() {
        assertThat(validate(new RestLayoutMetaBlock()
                .addSignaturesItem(createSignature())
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule()
                                .destinationSegmentName("segment 1")
                                .destinationStepName("step 1")
                                .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                                .pattern("pattern"))
                        .addKeysItem(new RestPublicKey()
                                .keyId("keyId")
                                .publicKey(new byte[]{1})
                        ).addLayoutSegmentsItem(new RestLayoutSegment()
                                .name("segment1")
                                .addStepsItem(new RestStep())
                        ))), contains(expectedErrors(
                                "layout.expectedEndProducts[0].destinationSegmentName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                                "layout.expectedEndProducts[0].destinationStepName", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                                "layout.keys[0].keyId", "must match \"^[0-9a-f]*$\"",
                                "layout.keys[0].keyId", "size must be between 64 and 64",
                                "layout.layoutSegments[0].steps[0].authorizedKeyIds", "size must be between 1 and 256",
                                "layout.layoutSegments[0].steps[0].name", "must not be null",
                                "layout.layoutSegments[0].steps[0].requiredNumberOfLinks", "must not be null")));
    }

    @Test
    void stepItemsRestLayout() {
        assertThat(validate(new RestLayoutMetaBlock()
                .addSignaturesItem(createSignature())
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule()
                                .destinationSegmentName("segment1")
                                .destinationStepName("step1")
                                .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                                .pattern("pattern"))
                        .addKeysItem(new RestPublicKey()
                                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                                .publicKey(new byte[]{1})
                        ).addLayoutSegmentsItem(new RestLayoutSegment()
                                .name("segment 1")
                                .addStepsItem(new RestStep()
                                        .addExpectedMaterialsItem(new RestRule())
                                        .addExpectedProductsItem(new RestRule())
                                        .name("step 1")
                                        .addAuthorizedKeyIdsItem("authorizedKeyId"))
                        ))), contains(expectedErrors(
                "layout.layoutSegments[0].name", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                "layout.layoutSegments[0].steps[0].expectedMaterials[0].pattern", "must not be null",
                "layout.layoutSegments[0].steps[0].expectedMaterials[0].ruleType", "must not be null",
                "layout.layoutSegments[0].steps[0].expectedProducts[0].pattern", "must not be null",
                "layout.layoutSegments[0].steps[0].expectedProducts[0].ruleType", "must not be null",
                "layout.layoutSegments[0].steps[0].name", "must match \"^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$\"",
                "layout.layoutSegments[0].steps[0].requiredNumberOfLinks", "must not be null"
        )));
    }

    @Test
    void stepItemsRestLayoutRules() {
        assertThat(validate(new RestLayoutMetaBlock()
                .addSignaturesItem(createSignature())
                .layout(new RestLayout()
                        .addAuthorizedKeyIdsItem("authorizedKeyId")
                        .addExpectedEndProductsItem(new RestMatchRule()
                                .destinationSegmentName("segment1")
                                .destinationStepName("step1")
                                .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                                .pattern("pattern"))
                        .addKeysItem(new RestPublicKey()
                                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                                .publicKey(new byte[]{1})
                        ).addLayoutSegmentsItem(new RestLayoutSegment()
                                .name("segment1")
                                .addStepsItem(new RestStep()
                                        .requiredNumberOfLinks(1)
                                        .addExpectedMaterialsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.MATCH).pattern("pattern"))
                                        .addExpectedProductsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.CREATE).pattern("pattern"))
                                        .name("step1")
                                        .addAuthorizedKeyIdsItem("authorizedKeyId"))
                        ))), empty()
        );
    }

    private RestSignature createSignature() {
        return new RestSignature()
                .hashAlgorithm(RestHashAlgorithm.SHA256)
                .keyAlgorithm(RestKeyAlgorithm.EC)
                .keyId("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254")
                .signature("c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254c8df0a497ab0df7136c4f97892f17914e6e5e021fdc039f0ea7c27d5a95c1254");
    }
}