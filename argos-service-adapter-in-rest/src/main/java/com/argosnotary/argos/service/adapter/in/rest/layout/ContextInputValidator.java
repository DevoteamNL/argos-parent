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
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification.TypeEnum;

import java.util.Set;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestValidationMessage.TypeEnum.DATA_INPUT;
import static com.argosnotary.argos.service.adapter.in.rest.layout.ValidationHelper.throwLayoutValidationException;

public abstract class ContextInputValidator {

    static <T extends ContextInputValidator> T of(TypeEnum type) {
        if (type == TypeEnum.XLDEPLOY) {
            return (T) new XLDeployContextInputValidator();
        }
        if (type == TypeEnum.GIT) {
            return (T) new GitContextInputValidator();
        }
        throw new IllegalArgumentException("context validator for collector type: " + type + "is not implemented");

    }

    private void checkForRequiredFieldsInContext(RestArtifactCollectorSpecification restArtifactCollectorSpecification, Set<String> requiredFields) {
        if (!restArtifactCollectorSpecification
                .getContext()
                .keySet()
                .containsAll(requiredFields)) {
            throwLayoutValidationException(DATA_INPUT, "context", "required fields : "
                    + requiredFields
                    +
                    " not present for collector type: " +
                    restArtifactCollectorSpecification.getType());
        }
    }

    void validateContextFields(RestArtifactCollectorSpecification restArtifactCollectorSpecification) {
        checkForRequiredFieldsInContext(restArtifactCollectorSpecification, requiredFields());
        checkFieldsForInputConsistencyRules(restArtifactCollectorSpecification);
    }

    protected abstract Set<String> requiredFields();

    protected abstract void checkFieldsForInputConsistencyRules(RestArtifactCollectorSpecification restArtifactCollectorSpecification);


}
