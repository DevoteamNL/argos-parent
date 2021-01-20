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
