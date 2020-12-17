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
package com.argosnotary.argos.domain.crypto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SignatureTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void equalsTest() {
        Signature sig1 = Signature.builder().keyId("keyId").signature("sig1").build();
        Signature sig2 = Signature.builder().keyId("keyId").signature("sig2").build();
        assertThat(sig1, is(sig2));
    }
    
    @Test
    void toStringTest() {
        Signature sig1 = Signature.builder().keyId("keyId").signature("sig1").build();
        assertThat(sig1.toString(), is("Signature(keyId=keyId, signature=sig1, keyAlgorithm=EC, hashAlgorithm=SHA384)"));
    }

}
