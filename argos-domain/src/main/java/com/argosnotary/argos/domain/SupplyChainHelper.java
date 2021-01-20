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
