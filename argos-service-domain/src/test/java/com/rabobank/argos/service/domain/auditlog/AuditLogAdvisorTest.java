/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.domain.auditlog;

import com.rabobank.argos.service.domain.util.reflection.ParameterData;
import com.rabobank.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.Builder;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogAdvisorTest {
    private static final String STRING_ARGUMENT_VALUE = "argumentValue";
    private static final String ARGUMENT_NAME = "argumentName";
    private static final String BEAN_NAME = "beanName";
    private static final String METHOD_NAME = "methodName";
    private static final String STRING_RETURN_VALUE = "stringReturnValue";
    private static final String VALUE_STRINGVALUE = "{\"value\":\"stringvalue\"}";
    private static final String FILTERED_VALUE = "{\"value\":\"value\"}";
    private static final String FILTER_BEAN_NAME = "filterBeanName";
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ReflectionHelper reflectionHelper;

    @Mock(lenient = true)
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature signature;

    @Mock
    private Method method;

    @Mock(lenient = true)
    private ArgumentSerializer argumentSerializer;

    @Mock
    private AuditLog auditLog;

    @Mock
    private AuditParam auditParam;

    @Captor
    private ArgumentCaptor<AuditLogData> serializerArgumentCaptor;

    private AuditLogAdvisor auditLogAdvisor;

    private static final Object[] STRING_ARGUMENT_VALUES = {STRING_ARGUMENT_VALUE};

    @Mock
    private ParameterData<AuditParam, Object> parameterData;

    @Mock
    private ObjectArgumentFilter<ArgumentValue> objectArgumentFilter;

    @BeforeEach
    void setup() {
        auditLogAdvisor = new AuditLogAdvisor(applicationContext, reflectionHelper);
        when(joinPoint.getSignature()).thenReturn(signature);


    }

    @Test
    void auditLogWithStringArgument() {
        when(joinPoint.getArgs()).thenReturn(STRING_ARGUMENT_VALUES);
        when(auditParam.value()).thenReturn(ARGUMENT_NAME);
        when(auditLog.argumentSerializerBeanName()).thenReturn(BEAN_NAME);
        when(applicationContext.getBean(BEAN_NAME, ArgumentSerializer.class)).thenReturn(argumentSerializer);
        when(parameterData.getAnnotation()).thenReturn(auditParam);
        when(parameterData.getValue()).thenReturn(STRING_ARGUMENT_VALUE);
        when(signature.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(METHOD_NAME);
        when(reflectionHelper.getParameterDataByAnnotation(method, AuditParam.class, STRING_ARGUMENT_VALUES)).thenReturn(Stream.of(parameterData));
        auditLogAdvisor.auditLog(joinPoint, auditLog, STRING_RETURN_VALUE);
        verify(argumentSerializer, times(1)).serialize(serializerArgumentCaptor.capture());
        AuditLogData auditLogData = serializerArgumentCaptor.getValue();
        assertThat(auditLogData.getMethodName(), is(METHOD_NAME));
        assertThat(auditLogData.getReturnValue(), is(STRING_RETURN_VALUE));
        assertThat(auditLogData.getArgumentData().isEmpty(), is(false));
        assertThat(auditLogData.getArgumentData().get(ARGUMENT_NAME), is(STRING_ARGUMENT_VALUE));
    }

    @Test
    void auditLogWithObjectArgument() {
        ArgumentValue argumentValue = ArgumentValue.builder().value("stringValue").build();
        Object[] argumentvalues = new Object[]{argumentValue};
        when(joinPoint.getArgs()).thenReturn(argumentvalues);
        when(auditParam.value()).thenReturn(ARGUMENT_NAME);
        when(auditParam.objectArgumentFilterBeanName()).thenReturn("");
        when(auditLog.argumentSerializerBeanName()).thenReturn(BEAN_NAME);
        when(applicationContext.getBean(BEAN_NAME, ArgumentSerializer.class)).thenReturn(argumentSerializer);
        when(parameterData.getAnnotation()).thenReturn(auditParam);
        when(parameterData.getValue()).thenReturn(argumentValue);
        when(signature.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(METHOD_NAME);
        when(argumentSerializer.serialize(argumentValue)).thenReturn(VALUE_STRINGVALUE);
        when(reflectionHelper.getParameterDataByAnnotation(ArgumentMatchers.any(), eq(AuditParam.class), ArgumentMatchers.any()))
                .thenReturn(Stream.of(parameterData));
        auditLogAdvisor.auditLog(joinPoint, auditLog, STRING_RETURN_VALUE);
        verify(argumentSerializer, times(2)).serialize(serializerArgumentCaptor.capture());
        AuditLogData auditLogData = serializerArgumentCaptor.getValue();
        assertThat(auditLogData.getMethodName(), is(METHOD_NAME));
        assertThat(auditLogData.getReturnValue(), is(STRING_RETURN_VALUE));
        assertThat(auditLogData.getArgumentData().isEmpty(), is(false));
        assertThat(auditLogData.getArgumentData().get(ARGUMENT_NAME), is(VALUE_STRINGVALUE));
    }

    @Test
    void auditLogWithObjectArgumentFilter() {
        ArgumentValue argumentValue = ArgumentValue.builder().value("stringValue").build();
        Object[] argumentvalues = new Object[]{argumentValue};
        Map<String, Object> filteredValues = new HashMap<>();
        filteredValues.put("value", "value");
        when(argumentSerializer.serialize(filteredValues)).thenReturn(FILTERED_VALUE);
        when(objectArgumentFilter.filterObjectArguments(argumentValue, auditParam)).thenReturn(filteredValues);
        when(joinPoint.getArgs()).thenReturn(argumentvalues);
        when(auditParam.value()).thenReturn(ARGUMENT_NAME);
        when(auditParam.objectArgumentFilterBeanName()).thenReturn(FILTER_BEAN_NAME);
        when(auditLog.argumentSerializerBeanName()).thenReturn(BEAN_NAME);
        when(applicationContext.getBean(BEAN_NAME, ArgumentSerializer.class)).thenReturn(argumentSerializer);
        when(applicationContext.getBean(FILTER_BEAN_NAME, ObjectArgumentFilter.class)).thenReturn(objectArgumentFilter);
        when(parameterData.getAnnotation()).thenReturn(auditParam);
        when(parameterData.getValue()).thenReturn(argumentValue);
        when(signature.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn(METHOD_NAME);
        when(argumentSerializer.serialize(argumentValue)).thenReturn(VALUE_STRINGVALUE);
        when(reflectionHelper.getParameterDataByAnnotation(ArgumentMatchers.any(), eq(AuditParam.class), ArgumentMatchers.any()))
                .thenReturn(Stream.of(parameterData));
        auditLogAdvisor.auditLog(joinPoint, auditLog, STRING_RETURN_VALUE);
        verify(argumentSerializer, times(2)).serialize(serializerArgumentCaptor.capture());
        AuditLogData auditLogData = serializerArgumentCaptor.getValue();
        assertThat(auditLogData.getMethodName(), is(METHOD_NAME));
        assertThat(auditLogData.getReturnValue(), is(STRING_RETURN_VALUE));
        assertThat(auditLogData.getArgumentData().isEmpty(), is(false));
        assertThat(auditLogData.getArgumentData().get(ARGUMENT_NAME), is(FILTERED_VALUE));
    }

    @Data
    @Builder
    public static class ArgumentValue {
        private String value;
    }
}