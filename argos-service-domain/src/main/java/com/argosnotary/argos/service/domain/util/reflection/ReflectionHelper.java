/*
 * Copyright (C) 2020 Argos Notary
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