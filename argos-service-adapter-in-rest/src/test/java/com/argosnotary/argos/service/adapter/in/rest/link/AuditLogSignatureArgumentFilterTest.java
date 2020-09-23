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
package com.argosnotary.argos.service.adapter.in.rest.link;

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogSignatureArgumentFilterTest {

    @Mock
    private RestLinkMetaBlock restLinkMetaBlock;

    @Mock
    private RestSignature restSignature;

    private AuditLogSignatureArgumentFilter auditLogSignatureArgumentFilter;

    @BeforeEach
    void setup() {
        auditLogSignatureArgumentFilter = new AuditLogSignatureArgumentFilter();

    }

    @Test
    void filterObjectArguments() {
        when(restLinkMetaBlock.getSignature()).thenReturn(restSignature);
        Map<String, Object> objectArguments = auditLogSignatureArgumentFilter.filterObjectArguments(restLinkMetaBlock);
        assertThat(objectArguments.containsKey("signature"), is(true));
        assertThat(objectArguments.get("signature"), sameInstance((restSignature)));
    }
}