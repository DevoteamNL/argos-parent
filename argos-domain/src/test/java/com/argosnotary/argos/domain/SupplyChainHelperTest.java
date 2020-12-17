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
