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
package com.argosnotary.argos.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SupplyChainHelper {
	
	private static final Pattern SUPPLY_CHAIN_PATH_REGEX = Pattern.compile("^((([a-z]|[a-z][a-z0-9\\-]{0,61}[a-z0-9])\\.)*([a-z]|[a-z][a-z0-9\\-]{0,61}[a-z0-9]):([a-z]|[a-z][a-z0-9-]*[a-z0-9]))$");
	private static final String NAME_NOT_CORRECT_MESSAGE = "[%s] not correct should be <label>.<label>:<supplyChainName> with hostname rules";

    public static List<String> reversePath(List<String> path) {
    	List<String> reversedPath = new ArrayList<>(path);
    	Collections.reverse(reversedPath);
    	return reversedPath;
    }

    public static String getSupplyChainName(String supplyChain) {
        if (!isCorrect(supplyChain)) {
            throw new ArgosError(String.format(NAME_NOT_CORRECT_MESSAGE, supplyChain));
        }
        return supplyChain.split(":")[1];
    }

    public static List<String> getSupplyChainPath(String supplyChain) {
        if (!isCorrect(supplyChain)) {
            throw new ArgosError(String.format(NAME_NOT_CORRECT_MESSAGE, supplyChain));
        }
        return new ArrayList<>(Arrays.asList(supplyChain.split(":")[0].split("\\.")));
    }
    
    private static boolean isCorrect(String supplyChain) {
        Matcher matcher = SUPPLY_CHAIN_PATH_REGEX.matcher(supplyChain);
        return matcher.matches();
    }
}
