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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class KeyIdProviderImplTest {

    @Test
    void computeKeyId() throws IOException, GeneralSecurityException {
        byte[] decode = Base64.getDecoder().decode(IOUtils.toByteArray(this.getClass().getResourceAsStream("/publickey")));
        String keyId = KeyIdProvider.computeKeyId(PublicKey.instance(decode));
        assertThat(keyId, is("a1d531635534c408a0286ce38423adc3da2cbaf1e635d98262db64cd858b0671"));        

        keyId = KeyIdProvider.computeKeyId(decode);
        assertThat(keyId, is("a1d531635534c408a0286ce38423adc3da2cbaf1e635d98262db64cd858b0671"));
    }
}
