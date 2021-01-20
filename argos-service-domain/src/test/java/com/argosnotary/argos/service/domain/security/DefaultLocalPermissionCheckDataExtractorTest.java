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

import com.argosnotary.argos.service.domain.util.reflection.ParameterData;
import com.argosnotary.argos.service.domain.util.reflection.ReflectionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultLocalPermissionCheckDataExtractorTest {

    private static final String LABEL_ID = "labelId";
    private static final String PARENT_LABEL_ID = "parentLabelId";
    private static final Object[] ARGUMENT_VALUES = {LABEL_ID, PARENT_LABEL_ID};
    private static final String EXTRACTOR = "extratcor";
    @Mock
    private ReflectionHelper reflectionHelper;
    @Mock
    private Method method;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ParameterData<LabelIdCheckParam, Object> parameterData;

    @Mock
    private LabelIdCheckParam labelIdCheckParam;

    @Mock
    private LabelIdExtractor labelIdExtractor;


    private DefaultLocalPermissionCheckDataExtractor extractor;

    @BeforeEach
    void setup() {
        extractor = new DefaultLocalPermissionCheckDataExtractor(reflectionHelper, applicationContext);
    }

    @Test
    void extractLocalPermissionCheckData() {
        when(reflectionHelper.getParameterDataByAnnotation(method, LabelIdCheckParam.class, ARGUMENT_VALUES)).thenReturn(Stream.of(parameterData));
        when(parameterData.getAnnotation()).thenReturn(labelIdCheckParam);
        when(parameterData.getValue()).thenReturn(PARENT_LABEL_ID);
        when(labelIdCheckParam.dataExtractor()).thenReturn(EXTRACTOR);
        when(applicationContext.getBean(EXTRACTOR, LabelIdExtractor.class)).thenReturn(labelIdExtractor);
        when(labelIdExtractor.extractLabelId(labelIdCheckParam, PARENT_LABEL_ID)).thenReturn(Optional.of(LABEL_ID));
        LocalPermissionCheckData checkData = extractor.extractLocalPermissionCheckData(method, ARGUMENT_VALUES);
        assertThat(checkData.getLabelIds(), contains(LABEL_ID));
    }
}