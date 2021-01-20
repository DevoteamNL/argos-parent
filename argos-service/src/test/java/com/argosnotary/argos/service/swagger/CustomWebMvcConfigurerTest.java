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
package com.argosnotary.argos.service.swagger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomWebMvcConfigurerTest {

    private CustomWebMvcConfigurer configurer;

    @Mock
    private ViewControllerRegistry registry;

    @Mock
    private ViewControllerRegistration viewControllerRegistration;

    @BeforeEach
    void setUp() {
        configurer = new CustomWebMvcConfigurer();
    }

    @Test
    void addViewControllers() {
        when(registry.addViewController(anyString())).thenReturn(viewControllerRegistration);
        configurer.addViewControllers(registry);

        verify(registry).addViewController("/swagger");
        verify(registry).addViewController("/swagger/");

        verify(viewControllerRegistration).setViewName("redirect:/swagger/");
        verify(viewControllerRegistration).setViewName("forward:/swagger/index.html");
    }
}