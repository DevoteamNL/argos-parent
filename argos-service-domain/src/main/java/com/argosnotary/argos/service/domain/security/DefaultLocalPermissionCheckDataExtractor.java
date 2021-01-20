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

import com.argosnotary.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.argosnotary.argos.service.domain.security.DefaultLocalPermissionCheckDataExtractor.DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;

@Component(DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME)
@RequiredArgsConstructor
public class DefaultLocalPermissionCheckDataExtractor implements LocalPermissionCheckDataExtractor {
    public static final String DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME = "defaultLocalPermissionCheckDataExtractor";
    private final ReflectionHelper reflectionHelper;

    private final ApplicationContext applicationContext;

    @Override
    public LocalPermissionCheckData extractLocalPermissionCheckData(Method method, Object[] argumentValues) {

        LocalPermissionCheckData.LocalPermissionCheckDataBuilder builder = LocalPermissionCheckData.builder();
        builder.labelIds(
                reflectionHelper.getParameterDataByAnnotation(method, LabelIdCheckParam.class, argumentValues)
                        .map(parameterData -> getValue(parameterData.getValue(), parameterData.getAnnotation()
                        )).flatMap(Optional::stream).collect(Collectors.toSet()));
        return builder.build();
    }

    private Optional<String> getValue(Object value, LabelIdCheckParam checkParam) {
        LabelIdExtractor labelIdExtractor = applicationContext.getBean(checkParam.dataExtractor(), LabelIdExtractor.class);
        return labelIdExtractor.extractLabelId(checkParam, value);
    }

}
