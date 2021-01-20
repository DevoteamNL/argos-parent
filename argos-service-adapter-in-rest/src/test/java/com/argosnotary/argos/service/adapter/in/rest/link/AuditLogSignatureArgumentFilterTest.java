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