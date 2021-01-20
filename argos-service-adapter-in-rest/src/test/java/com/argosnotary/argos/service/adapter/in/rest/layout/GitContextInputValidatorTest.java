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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification.TypeEnum;
import static com.argosnotary.argos.service.adapter.in.rest.layout.RandomStringHelper.getAlphaNumericString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitContextInputValidatorTest {

    private GitContextInputValidator gitContextInputValidator;
    @Mock
    private RestArtifactCollectorSpecification restArtifactCollectorSpecification;


    @BeforeEach
    void setup() {
        gitContextInputValidator = ContextInputValidator.of(TypeEnum.GIT);
    }

    @Test
    void validateContextFieldsWithNoRequiredFieldShouldThrowException() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Collections.emptyMap());
        when(restArtifactCollectorSpecification.getType()).thenReturn(TypeEnum.GIT);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> gitContextInputValidator.validateContextFields(restArtifactCollectorSpecification));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("context"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("required fields : [repository] not present for collector type: GIT"));
    }

    @Test
    void validateContextFieldsWithInValidInputFieldShouldThrowException() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Map.of("repository", "xlde*ploy"));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> gitContextInputValidator.validateContextFields(restArtifactCollectorSpecification));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("repository"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("repository field contains invalid characters"));
    }

    @Test
    void validateContextFieldsWithTooLongCharacterValueShouldThrowException() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Map.of("repository", getAlphaNumericString(256)));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> gitContextInputValidator.validateContextFields(restArtifactCollectorSpecification));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("repository"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("repository name is too long 256 only 255 is allowed"));
    }


    @Test
    void validateContextFieldsWithRequiredFields() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Map.of("repository", "argos"));
        gitContextInputValidator.validateContextFields(restArtifactCollectorSpecification);
        verify(restArtifactCollectorSpecification, times(2)).getContext();
    }


}