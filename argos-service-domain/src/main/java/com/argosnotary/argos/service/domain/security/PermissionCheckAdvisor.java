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
