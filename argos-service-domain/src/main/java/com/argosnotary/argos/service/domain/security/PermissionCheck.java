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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.argosnotary.argos.service.domain.security.DefaultLocalPermissionCheckDataExtractor.DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;
import static com.argosnotary.argos.service.domain.security.DefaultLocalPermissionCheckStrategy.DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionCheck {

    Permission[] permissions() default {};

    String localPermissionDataExtractorBean() default DEFAULT_LOCAL_PERMISSION_CHECK_DATA_EXTRACTOR_BEAN_NAME;

    String localPermissionCheckStrategyBean() default DEFAULT_LOCAL_PERMISSION_CHECK_STRATEGY_BEAN_NAME;

}