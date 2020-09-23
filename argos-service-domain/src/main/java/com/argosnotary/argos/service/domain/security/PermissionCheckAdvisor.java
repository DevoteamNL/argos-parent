/*
 * Copyright (C) 2020 Argos Notary Cooperative
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
package com.argosnotary.argos.service.domain.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(value = 1)
public class PermissionCheckAdvisor {

    private final AccountSecurityContext accountSecurityContext;

    private final ApplicationContext applicationContext;

    @Pointcut("@annotation(permissionCheck)")
    public void permissionCheckPointCut(PermissionCheck permissionCheck) {
        //This is an AspectJ pointcut implemented as method
    }

    @Before(value = "permissionCheckPointCut(permissionCheck)", argNames = "joinPoint,permissionCheck")
    public void checkPermissions(JoinPoint joinPoint, PermissionCheck permissionCheck) {
        log.info("checking of method:{} with permissions {}",
                joinPoint.getSignature().getName(),
                permissionCheck.permissions()
        );

        if (!(hasGlobalPermissions(permissionCheck) || hasLocalPermissions(joinPoint, permissionCheck))) {
            log.info("access denied for method:{} with permissions {}",
                    joinPoint.getSignature().getName(),
                    permissionCheck.permissions()
            );
            throw new AccessDeniedException("Access denied");
        }
    }

    private boolean hasGlobalPermissions(PermissionCheck permissionCheck) {
        return accountSecurityContext.getGlobalPermission()
                .stream()
                .anyMatch(asList(permissionCheck.permissions())::contains);

    }

    private boolean hasLocalPermissions(JoinPoint joinPoint, PermissionCheck permissionCheck) {

        LocalPermissionCheckDataExtractor localPermissionCheckDataExtractor = applicationContext
                .getBean(permissionCheck.localPermissionDataExtractorBean(), LocalPermissionCheckDataExtractor.class);
        LocalPermissionCheckStrategy localPermissionCheckStrategy = applicationContext.getBean(permissionCheck.localPermissionCheckStrategyBean(), LocalPermissionCheckStrategy.class);
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] argumentValues = joinPoint.getArgs();
        LocalPermissionCheckData labelCheckData = localPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, argumentValues);
        return localPermissionCheckStrategy.hasLocalPermission(labelCheckData, new HashSet<>(List.of(permissionCheck.permissions())));
    }
}
