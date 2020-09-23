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
package com.argosnotary.argos.domain.crypto.signing;

import java.security.GeneralSecurityException;

import com.argosnotary.argos.domain.crypto.HashAlgorithm;
import com.argosnotary.argos.domain.crypto.KeyAlgorithm;

public enum SignatureAlgorithm {
	SHA_384_WITH_ECDSA("SHA384withECDSA"), SHA_256_WITH_RSA("SHA256withRSA");;
	
	String stringValue;
	
	SignatureAlgorithm(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public String getStringValue() {
	    return stringValue;
	}
	
	public static SignatureAlgorithm getAlgorithm(KeyAlgorithm keyAlgorithm, HashAlgorithm hashAlgorithm) throws GeneralSecurityException {
    	if (KeyAlgorithm.EC == keyAlgorithm && HashAlgorithm.SHA384 == hashAlgorithm) {
    		return SHA_384_WITH_ECDSA;
    	} else {
    	    throw new GeneralSecurityException(String.format("Combination of algorithms [%s] and [%s] not supported", keyAlgorithm, hashAlgorithm));
    	}
	}

}
