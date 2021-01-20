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

import com.argosnotary.argos.domain.permission.Permission;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionCheckAdvisorTest {

    private static final String ROLE_ID = "roleId";
    private static final String CHECK_BEAN = "checkBean";
    private static final String EXTRACTOR_BEAN = "extraBean";
    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Mock
    private ApplicationContext applicationContext;

    private PermissionCheckAdvisor advisor;

    @Mock(lenient = true)
    private JoinPoint joinPoint;

    @Mock
    private PermissionCheck permissionCheck;


    @Mock
    private MethodSignature signature;

    @Mock
    private LocalPermissionCheckDataExtractor localPermissionCheckDataExtractor;

    @Mock
    private LocalPermissionCheckStrategy localPermissionCheckStrategy;

    @Mock
    private Method method;

    @Mock
    private LocalPermissionCheckData checkData;

    @BeforeEach
    void setUp() {
        advisor = new PermissionCheckAdvisor(accountSecurityContext, applicationContext);
        when(joinPoint.getSignature()).thenReturn(signature);
    }

    @Test
    void checkPermissionsHasGlobalPermission() {
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.READ, Permission.LOCAL_PERMISSION_EDIT));
        advisor.checkPermissions(joinPoint, permissionCheck);
    }

    @Test
    void checkPermissionsHasMultipleGlobalPermission() {
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.READ});
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Set.of(Permission.READ, Permission.LOCAL_PERMISSION_EDIT));
        advisor.checkPermissions(joinPoint, permissionCheck);
    }

    @Test
    void checkPermissionsHasWrongGlobalPermission() {
        mockPermissionCheck();
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> advisor.checkPermissions(joinPoint, permissionCheck));
        MatcherAssert.assertThat(accessDeniedException.getMessage(), is("Access denied"));
    }

    @Test
    void checkPermissionsHasNoGlobalPermission() {
        mockPermissionCheck();
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Collections.emptySet());
        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> advisor.checkPermissions(joinPoint, permissionCheck));
        MatcherAssert.assertThat(accessDeniedException.getMessage(), is("Access denied"));
    }

    @Test
    void checkPermissionsSaHasNoGlobalPermission() {
        mockPermissionCheck();
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Collections.emptySet());
        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class, () -> advisor.checkPermissions(joinPoint, permissionCheck));
        MatcherAssert.assertThat(accessDeniedException.getMessage(), is("Access denied"));
    }

    @Test
    void checkPermissionsHasLocalPermission() {
        mockPermissionCheck();
        when(signature.getMethod()).thenReturn(method);
        when(permissionCheck.permissions()).thenReturn(new Permission[]{Permission.LOCAL_PERMISSION_EDIT});
        when(accountSecurityContext.getGlobalPermission()).thenReturn(Collections.emptySet());
        Object[] args = new Object[]{};
        when(joinPoint.getArgs()).thenReturn(args);
        when(localPermissionCheckDataExtractor.extractLocalPermissionCheckData(method, args)).thenReturn(checkData);
        when(localPermissionCheckStrategy.hasLocalPermission(checkData, new HashSet<>(List.of(Permission.LOCAL_PERMISSION_EDIT)))).thenReturn(true);
        advisor.checkPermissions(joinPoint, permissionCheck);
    }

    private void mockPermissionCheck() {
        when(permissionCheck.localPermissionCheckStrategyBean()).thenReturn(CHECK_BEAN);
        when(permissionCheck.localPermissionDataExtractorBean()).thenReturn(EXTRACTOR_BEAN);
        when(applicationContext.getBean(EXTRACTOR_BEAN, LocalPermissionCheckDataExtractor.class)).thenReturn(localPermissionCheckDataExtractor);
        when(applicationContext.getBean(CHECK_BEAN, LocalPermissionCheckStrategy.class)).thenReturn(localPermissionCheckStrategy);
    }
}