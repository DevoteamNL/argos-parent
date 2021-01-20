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
package com.argosnotary.argos.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SupplyChainHelperTest {

    @Test
    void getSupplyChainNameShouldGiveName() {
        assertThat(SupplyChainHelper.getSupplyChainName("label-1.label-2:name"), is("name"));
    }
    
    @Test
    void getSupplyChainNameShouldGiveException() {
    	Throwable exception = assertThrows(ArgosError.class, () -> {
    		SupplyChainHelper.getSupplyChainName("label_1.label_2:name");
          });
    	assertEquals("[label_1.label_2:name] not correct should be <label>.<label>:<supplyChainName> with hostname rules", exception.getMessage());
    }
    
    @Test
    void getSupplyChainPathShouldReturnPath() {
        assertThat(SupplyChainHelper.getSupplyChainPath("label-1.label-2:name"), is(Arrays.asList("label-1", "label-2")));
    }
    
    @Test
    void getSupplyChainPathShouldGiveException() {
        Throwable exception = assertThrows(ArgosError.class, () -> {
            SupplyChainHelper.getSupplyChainPath("label_1.label_2:name");
          });
        assertEquals("[label_1.label_2:name] not correct should be <label>.<label>:<supplyChainName> with hostname rules", exception.getMessage());
    }
    
    @Test
    void getSupplyChainNameShouldGiveLabelToLongException() {
        Throwable exception = assertThrows(ArgosError.class, () -> {
            SupplyChainHelper.getSupplyChainName("l0123456789012345678901234567890123456789012345678901234567890123456789.label2:name");
          });
        assertEquals("[l0123456789012345678901234567890123456789012345678901234567890123456789.label2:name] not correct should be <label>.<label>:<supplyChainName> with hostname rules", exception.getMessage());
    }
    
    @Test
    void getSupplyChainReversePathShouldReturnReversePath() {
        assertThat(SupplyChainHelper.reversePath(SupplyChainHelper.getSupplyChainPath("label-1.label-2:name")), is(Arrays.asList("label-2", "label-1")));
    }
    
    @Test
    void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      Constructor<SupplyChainHelper> constructor = SupplyChainHelper.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers()), is(true));
      constructor.setAccessible(true);
      constructor.newInstance();
    }
}
