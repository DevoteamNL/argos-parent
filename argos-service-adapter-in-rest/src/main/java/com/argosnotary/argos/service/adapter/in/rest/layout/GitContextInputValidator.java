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
