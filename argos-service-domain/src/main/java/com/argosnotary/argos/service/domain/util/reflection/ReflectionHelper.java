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

import org.springframework.stereotype.Component;

import com.codepoetics.protonpack.StreamUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class ReflectionHelper {

    public <T extends Annotation, S> Stream<ParameterData<T, S>> getParameterDataByAnnotation(Method method, Class<T> annotation, S[] argumentValues) {
        return StreamUtils
                .zipWithIndex(Arrays.stream(method.getParameters()))
                .filter(p -> p.getValue().getAnnotation(annotation) != null)
                .map(p -> new ParameterData<>(p.getValue().getAnnotation(annotation), argumentValues[(int) p.getIndex()]));
    }
}