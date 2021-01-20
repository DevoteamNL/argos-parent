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
package com.argosnotary.argos.service.domain.util.reflection;

import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class ReflectionHelperTest {

    public static final String LABEL_ID = "labelId";

    @Test
    void getParameterDataByAnnotation() throws NoSuchMethodException {
        Stream<ParameterData<LabelIdCheckParam, Object>> data = new ReflectionHelper().getParameterDataByAnnotation(TestClass.class.getMethod("test", String.class, String.class), LabelIdCheckParam.class, new Object[]{"arg1", LABEL_ID});
        ParameterData<LabelIdCheckParam, Object> labelIdCheckParamObjectParameterData = data.findFirst().orElseThrow();
        assertThat(labelIdCheckParamObjectParameterData.getValue(), is(LABEL_ID));
        assertThat(labelIdCheckParamObjectParameterData.getAnnotation().propertyPath(), is(""));
    }

    @Test
    void getParameterDataByAnnotationNotFound() throws NoSuchMethodException {
        assertThat(new ReflectionHelper().getParameterDataByAnnotation(TestClass.class.getMethod("testWithoutAnnotation", String.class, String.class), LabelIdCheckParam.class, new Object[]{"arg1", LABEL_ID}).collect(Collectors.toList()), empty());
    }

    private class TestClass {
        public void test(String arg1, @LabelIdCheckParam String labelId) {

        }

        public void testWithoutAnnotation(String arg1, String labelId) {

        }
    }
}