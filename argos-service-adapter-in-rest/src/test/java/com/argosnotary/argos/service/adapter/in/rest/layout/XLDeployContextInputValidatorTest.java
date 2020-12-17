/*
 * Copyright (C) 2020 Argos Notary
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static com.argosnotary.argos.service.adapter.in.rest.layout.RandomStringHelper.getAlphaNumericString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XLDeployContextInputValidatorTest {

    private XLDeployContextInputValidator xlDeployContextInputValidator;
    @Mock
    private RestArtifactCollectorSpecification restArtifactCollectorSpecification;

    @BeforeEach
    void setup() {
        xlDeployContextInputValidator = ContextInputValidator.of(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY);
    }

    @Test
    void validateContextFieldsWithNoRequiredFieldsShouldThrowException() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Collections.emptyMap());
        when(restArtifactCollectorSpecification.getType()).thenReturn(RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> xlDeployContextInputValidator.validateContextFields(restArtifactCollectorSpecification));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("context"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("required fields : [applicationName] not present for collector type: XLDEPLOY"));

    }

    @Test
    void validateContextFieldsWithInvalidCharacterValueShouldThrowException() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Map.of("applicationName", "xlde*ploy"));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> xlDeployContextInputValidator.validateContextFields(restArtifactCollectorSpecification));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("applicationName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("(no `/`, `\\`, `:`, `[`, `]`, `|`, `,` or `*`) characters are allowed"));

    }

    @Test
    void validateContextFieldsWithTooLongCharacterValueShouldThrowException() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Map.of("applicationName", getAlphaNumericString(256)));
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> xlDeployContextInputValidator.validateContextFields(restArtifactCollectorSpecification));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("applicationName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("applicationName is to long 256 only 255 is allowed"));

    }

    @Test
    void validateContextFieldsWithRequiredFields() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(Map.of("applicationName", "xldeploy"));
        xlDeployContextInputValidator.validateContextFields(restArtifactCollectorSpecification);
        verify(restArtifactCollectorSpecification, times(2)).getContext();
    }
}