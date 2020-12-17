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
package com.argosnotary.argos.service.domain.auditlog;

import com.argosnotary.argos.domain.SupplyChainHelper;
import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.security.LocalPermissionCheckData;
import com.argosnotary.argos.service.domain.security.LocalPermissionCheckDataExtractor;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import com.argosnotary.argos.service.domain.util.reflection.ReflectionHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j(topic = "argos.AuditLog")
@RequiredArgsConstructor
@Order(value = 2)
public class AuditLogAdvisor {
    public static final String ARGOS_AUDIT_LOG = "argos.AuditLog";
    private final ApplicationContext applicationContext;

    private final ReflectionHelper reflectionHelper;

    private final HierarchyRepository hierarchyRepository;

    @Pointcut("@annotation(auditLog)")
    public void auditLogPointCut(AuditLog auditLog) {
        //This is an AspectJ pointcut implemented as method
    }

    @AfterReturning(value = "auditLogPointCut(auditLog)", argNames = "joinPoint,auditLog,returnValue", returning = "returnValue")
    public void auditLog(JoinPoint joinPoint, AuditLog auditLog, Object returnValue) {

        ArgumentSerializer argumentSerializer = applicationContext
                .getBean(auditLog.argumentSerializerBeanName(), ArgumentSerializer.class);
        Object[] argumentValues = joinPoint.getArgs();
        String serializedReturnValue = serializeValue(returnValue, argumentSerializer, null);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        List<String> paths = resolveOptionalPaths(argumentValues, method);
        Map<String, String> parameterValueMap = reflectionHelper.getParameterDataByAnnotation(method, AuditParam.class, argumentValues)
                .collect(Collectors
                        .toMap(p -> p.getAnnotation().value(),
                                p -> serializeValue(p.getValue(), argumentSerializer, p.getAnnotation())
                        )
                );
        AuditLogData auditLogData = AuditLogData.builder()
                .argumentData(parameterValueMap)
                .methodName(method.getName())
                .returnValue(serializedReturnValue)
                .paths(paths)
                .build();
        log.info("AuditLog: {}", argumentSerializer.serialize(auditLogData));
    }

    private List<String> resolveOptionalPaths(Object[] argumentValues, Method method) {
        List<String> paths = new ArrayList<>();
        Optional<PermissionCheck> optionalPermissionCheck = Optional.ofNullable(method.getAnnotation(PermissionCheck.class));
        optionalPermissionCheck.ifPresent(permissionCheck -> {
            LocalPermissionCheckDataExtractor localPermissionCheckDataExtractor = applicationContext
                    .getBean(permissionCheck.localPermissionDataExtractorBean(), LocalPermissionCheckDataExtractor.class);
            LocalPermissionCheckData labelCheckData = localPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, argumentValues);
            labelCheckData.getLabelIds().forEach(labelId -> hierarchyRepository.getSubTree(labelId, HierarchyMode.NONE, 0)
                    .ifPresent(treeNode -> paths.add(SupplyChainHelper
                            .reversePath(treeNode.getPathToRoot())
                            .stream()
                            .collect(Collectors.joining("/")) + "/" + treeNode.getName())
                    ));
        });
        return paths;
    }

    private String serializeValue(Object argumentValue, ArgumentSerializer argumentSerializer, @Nullable AuditParam auditParam) {
        if (argumentValue instanceof String) {
            return (String) argumentValue;
        } else {
            if (hasObjectArgumentFilter(auditParam)) {
                ObjectArgumentFilter<Object> objectArgumentFilter = applicationContext
                        .getBean(auditParam.objectArgumentFilterBeanName(), ObjectArgumentFilter.class);
                return argumentSerializer.serialize(objectArgumentFilter.filterObjectArguments(argumentValue));
            } else {
                return argumentSerializer.serialize(argumentValue);
            }

        }
    }

    private boolean hasObjectArgumentFilter(@Nullable AuditParam auditParam) {
        return auditParam != null && !"".equals(auditParam.objectArgumentFilterBeanName());
    }
}
