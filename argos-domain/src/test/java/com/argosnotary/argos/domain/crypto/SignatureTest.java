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
