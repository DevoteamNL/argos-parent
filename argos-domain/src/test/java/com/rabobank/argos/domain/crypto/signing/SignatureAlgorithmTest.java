/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.domain.crypto.signing;

import static org.junit.jupiter.api.Assertions.*;

import java.security.GeneralSecurityException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rabobank.argos.domain.crypto.HashAlgorithm;
import com.rabobank.argos.domain.crypto.KeyAlgorithm;

class SignatureAlgorithmTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void getAlgorithmTest() throws GeneralSecurityException {
        Throwable exception = assertThrows(GeneralSecurityException.class, () -> {
            SignatureAlgorithm.getAlgorithm(KeyAlgorithm.EC, HashAlgorithm.SHA256);
          });
        assertEquals("Combination of algorithms [EC] and [SHA256] not supported", exception.getMessage());
    }

}
