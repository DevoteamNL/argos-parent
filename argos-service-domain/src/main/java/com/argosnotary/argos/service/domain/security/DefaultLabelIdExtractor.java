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
import org.apache.commons.beanutils.BeanUtilsBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@Component(DefaultLabelIdExtractor.DEFAULT_LABEL_ID_EXTRACTOR)
public class DefaultLabelIdExtractor implements LabelIdExtractor {
    public static final String DEFAULT_LABEL_ID_EXTRACTOR = "defaultLabelIdExtractor";

    @Override
    public Optional<String> extractLabelId(LabelIdCheckParam checkParam, Object value) {
        return Optional.ofNullable(getValue(value, checkParam.propertyPath()));
    }

    private String getValue(Object value, String path) {
        if (StringUtils.isEmpty(path)) {
            return (String) value;
        } else {
            try {
                return BeanUtilsBean.getInstance().getProperty(value, path);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new ArgosError(e.getMessage(), e);
            }
        }
    }
}
