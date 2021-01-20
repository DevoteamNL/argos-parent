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
import java.util.regex.Pattern;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static com.argosnotary.argos.service.adapter.in.rest.layout.ValidationHelper.throwLayoutValidationException;

public class GitContextInputValidator extends ContextInputValidator {

    private static final int MAX_LENGTH = 255;
    private static final String REPOSITORY_NAME = "repository";
    private static final Pattern INVALID_CHAR = Pattern.compile("^[A-Za-z0-9_.\\-/]*$");


    @Override
    protected Set<String> requiredFields() {
        return Set.of(REPOSITORY_NAME);
    }

    @Override
    protected void checkFieldsForInputConsistencyRules(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        String repositoryNameValue = restArtifactCollectorSpecification.getContext().get(REPOSITORY_NAME);
        if (!INVALID_CHAR.matcher(repositoryNameValue).matches()) {
            throwLayoutValidationException(DATA_INPUT, REPOSITORY_NAME,
                    "repository field contains invalid characters");
        }
        if (repositoryNameValue.length() > MAX_LENGTH) {
            throwLayoutValidationException(DATA_INPUT, REPOSITORY_NAME,
                    "repository name is too long "
                            + repositoryNameValue.length() +
                            " only "
                            + MAX_LENGTH + " is allowed");
        }
    }
}
