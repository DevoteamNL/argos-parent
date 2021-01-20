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
package com.argosnotary.argos.service.domain.security;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.hierarchy.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLabelIdExtractorTest {

    private static final String VALUE = "value";

    private DefaultLabelIdExtractor extractor;

    @Mock
    private LabelIdCheckParam checkParam;

    @BeforeEach
    void setUp() {
        extractor = new DefaultLabelIdExtractor();
    }

    @Test
    void extractLabelId() {
        assertThat(extractor.extractLabelId(checkParam, VALUE), is(Optional.of(VALUE)));
    }

    @Test
    void extractLabelFromObject() {
        when(checkParam.propertyPath()).thenReturn("labelId");
        assertThat(extractor.extractLabelId(checkParam, Label.builder().labelId(VALUE).build()), is(Optional.of(VALUE)));
    }

    @Test
    void extractLabelFromObjectMethodNotExists() {
        when(checkParam.propertyPath()).thenReturn("other");
        Label label = Label.builder().labelId(VALUE).build();
        ArgosError argosError = assertThrows(ArgosError.class, () -> extractor.extractLabelId(checkParam, label));
        assertThat(argosError.getMessage(), is("Unknown property 'other' on class 'class com.argosnotary.argos.domain.hierarchy.Label'"));
    }

}