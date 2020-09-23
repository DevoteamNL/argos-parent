/*
 * Copyright (C) 2020 Argos Notary Cooperative
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

import java.security.GeneralSecurityException;

import com.argosnotary.argos.domain.crypto.signing.SignatureAlgorithm;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@EqualsAndHashCode(exclude={"signature"})
@ToString
public class Signature {
    private String keyId;
    private String signature;
    @Builder.Default
    private KeyAlgorithm keyAlgorithm = KeyAlgorithm.EC;
    @Builder.Default
    private HashAlgorithm hashAlgorithm = HashAlgorithm.SHA384;
    
    public SignatureAlgorithm getAlgorithm() throws GeneralSecurityException {
    	return SignatureAlgorithm.getAlgorithm(this.keyAlgorithm, this.hashAlgorithm);
    }
}
