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
package com.argosnotary.argos.service.adapter.in.rest.layout;

import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static com.argosnotary.argos.service.adapter.in.rest.layout.ValidationHelper.throwLayoutValidationException;

public class XLDeployContextInputValidator extends ContextInputValidator {


    private static final String APPLICATION_NAME = "applicationName";
    //(no `/`, `\`, `:`, `[`, `]`, `|`, `,` or `*`)
    private static final Pattern invalidCharacters = Pattern.compile("[/\\\\:\\[\\]|,*\\]]");
    private static final int MAX_LENGTH = 255;

    XLDeployContextInputValidator() {
    }

    @Override
    protected Set<String> requiredFields() {
        return Set.of(APPLICATION_NAME);
    }

    @Override
    protected void checkFieldsForInputConsistencyRules(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        String applicationNameValue = restArtifactCollectorSpecification.getContext().get(APPLICATION_NAME);
        Matcher m = invalidCharacters.matcher(applicationNameValue);
        if (m.find()) {
            throwLayoutValidationException(DATA_INPUT, APPLICATION_NAME,
                    "(no `/`, `\\`, `:`, `[`, `]`, `|`, `,` or `*`) characters are allowed");
        }
        if (applicationNameValue.length() > MAX_LENGTH) {
            throwLayoutValidationException(DATA_INPUT, APPLICATION_NAME,
                    "applicationName is to long "
                            + applicationNameValue.length() +
                            " only "
                            + MAX_LENGTH + " is allowed");

        }
    }
}
