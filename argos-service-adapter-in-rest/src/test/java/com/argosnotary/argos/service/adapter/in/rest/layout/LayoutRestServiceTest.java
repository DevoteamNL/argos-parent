/*
 * Copyright (C) 2020 Argos Notary Coöperatie UA
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

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.Account;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.Layout;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.layout.LayoutSegment;
import com.argosnotary.argos.domain.layout.ReleaseConfiguration;
import com.argosnotary.argos.domain.layout.Step;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestApprovalConfiguration;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayout;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestReleaseConfiguration;
import com.argosnotary.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.layout.ReleaseConfigurationRepository;
import com.argosnotary.argos.service.domain.security.AccountSecurityContext;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.argosnotary.argos.service.adapter.in.rest.api.model.RestArtifactCollectorSpecification.TypeEnum.XLDEPLOY;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LayoutRestServiceTest {

    private static final String SEGMENT_NAME = "segmentName";
    private static final String STEP_NAME = "stepName";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";

    @Mock
    private LayoutMetaBlockMapper converter;

    @Mock
    private LayoutMetaBlockRepository layoutMetaBlockRepository;

    @Mock
    private RestLayoutMetaBlock restLayoutMetaBlock;

    @Mock
    private ApprovalConfigurationRepository approvalConfigurationRepository;

    @Mock
    private ConfigurationMapper configurationMapper;

    @Mock
    private ReleaseConfigurationRepository releaseConfigurationRepository;

    @Mock
    private RestLayout restLayout;

    @Mock
    private Layout layout;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private HttpServletRequest httpServletRequest;

    private LayoutRestService service;

    @Mock
    private LayoutValidatorService validator;

    @Mock
    private RestApprovalConfiguration restApprovalConfiguration;

    @Mock
    private ApprovalConfiguration approvalConfiguration;
    @Mock
    private RestArtifactCollectorSpecification restArtifactCollectorSpecification;

    @Mock
    private AccountSecurityContext accountSecurityContext;

    @Mock
    private Account account;

    @Mock
    private KeyPair keyPair;

    @Mock
    private LayoutSegment layoutSegment;

    @Mock
    private ReleaseConfiguration releaseConfiguration;

    @Mock
    private RestReleaseConfiguration restReleaseConfiguration;

    @Mock
    private Step step;

    @BeforeEach
    void setUp() {
        service = new LayoutRestService(converter, layoutMetaBlockRepository, validator, approvalConfigurationRepository, releaseConfigurationRepository, configurationMapper, accountSecurityContext);
    }

    @Test
    void createOrUpdateLayout() {
        ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(httpServletRequest);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock)).thenReturn(layoutMetaBlock);
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.createOrUpdateLayout(SUPPLY_CHAIN_ID, restLayoutMetaBlock);
        assertThat(responseEntity.getStatusCodeValue(), is(201));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
        assertThat(Objects.requireNonNull(responseEntity.getHeaders().getLocation()).getPath(), is(""));
        verify(layoutMetaBlockRepository).createOrUpdate(layoutMetaBlock);
        verify(validator).validate(layoutMetaBlock);

    }

    @Test
    void validateLayoutValid() {
        when(converter.convertFromRestLayout(restLayout)).thenReturn(layout);
        ResponseEntity responseEntity = service.validateLayout(SUPPLY_CHAIN_ID, restLayout);
        assertThat(responseEntity.getStatusCodeValue(), is(204));
        verify(validator).validateLayout(layout);
    }

    @Test
    void getLayout() {
        when(converter.convertToRestLayoutMetaBlock(layoutMetaBlock)).thenReturn(restLayoutMetaBlock);
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        ResponseEntity<RestLayoutMetaBlock> responseEntity = service.getLayout(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), sameInstance(restLayoutMetaBlock));
    }

    @Test
    void getLayoutNotFound() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> service.getLayout(SUPPLY_CHAIN_ID));
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getReason(), is("layout not found"));
    }

    @Test
    void createApprovalConfigurationShouldStoreLayout() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(createSegmentAndStep());
        when(approvalConfiguration.getSegmentName()).thenReturn(SEGMENT_NAME);
        when(approvalConfiguration.getStepName()).thenReturn(STEP_NAME);
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        when(configurationMapper.convertToRestApprovalConfiguration(approvalConfiguration))
                .thenReturn(restApprovalConfiguration);
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.
                createApprovalConfigurations(SUPPLY_CHAIN_ID, List.of(restApprovalConfiguration));
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        verify(approvalConfigurationRepository).saveAll(SUPPLY_CHAIN_ID, List.of(approvalConfiguration));
        verify(approvalConfiguration).setSupplyChainId(SUPPLY_CHAIN_ID);
    }

    @Test
    void createApprovalConfigurationWithIncorrectSegmentNameShouldThrowValidationError() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(createSegmentAndStep());
        when(approvalConfiguration.getSegmentName()).thenReturn("wrong-segment");
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () ->
                service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs)
        );

        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("segmentName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("segment with name : wrong-segment does not exist in layout"));
    }

    @Test
    void createApprovalConfigurationsWithIncorrectStepNameShouldThrowValidationError() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);

        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(createSegmentAndStep());
        when(approvalConfiguration.getSegmentName()).thenReturn(SEGMENT_NAME);
        when(approvalConfiguration.getStepName()).thenReturn("wrong-stepname");
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);

        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs));

        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("stepName"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("step with name: wrong-stepname in segment: segmentName does not exist in layout"));
    }


    @Test
    void createApprovalConfigurationsWithoutExistingLayoutShouldThrowValidationError() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        when(approvalConfiguration.getSupplyChainId()).thenReturn(SUPPLY_CHAIN_ID);
        when(configurationMapper.convertFromRestApprovalConfiguration(restApprovalConfiguration))
                .thenReturn(approvalConfiguration);
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);

        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () ->
                service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs)
        );
        assertThat(responseStatusException.getStatus(), is(HttpStatus.NOT_FOUND));
        assertThat(responseStatusException.getMessage(), is("404 NOT_FOUND \"layout not found\""));
    }


    @Test
    void createApprovalConfigurationsWithIncorrectArtifactSpecificationShouldThrowValidationError() {
        when(restArtifactCollectorSpecification.getContext()).thenReturn(emptyMap());
        when(restArtifactCollectorSpecification.getType()).thenReturn(XLDEPLOY);
        when(restApprovalConfiguration.getArtifactCollectorSpecifications()).thenReturn(singletonList(restArtifactCollectorSpecification));
        List<RestApprovalConfiguration> configs = List.of(restApprovalConfiguration);
        
        LayoutValidationException layoutValidationException = assertThrows(LayoutValidationException.class, () -> service.createApprovalConfigurations(SUPPLY_CHAIN_ID, configs));
        assertThat(layoutValidationException.getValidationMessages().isEmpty(), is(false));
        assertThat(layoutValidationException.getValidationMessages().get(0).getField(), is("context"));
        assertThat(layoutValidationException.getValidationMessages().get(0).getMessage(), is("required fields : [applicationName] not present for collector type: XLDEPLOY"));
    }


    @Test
    void getApprovalConfigurations() {
        when(approvalConfigurationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(approvalConfiguration));
        when(configurationMapper.convertToRestApprovalConfiguration(approvalConfiguration))
                .thenReturn(restApprovalConfiguration);
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalConfigurations(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody(), hasSize(1));
    }

    @Test
    void getApprovalsForAccount() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(layoutMetaBlock));
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(List.of(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(List.of(step));


        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(account.getActiveKeyPair()).thenReturn(keyPair);

        when(approvalConfigurationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(List.of(approvalConfiguration));
        when(configurationMapper.convertToRestApprovalConfiguration(approvalConfiguration))
                .thenReturn(restApprovalConfiguration);


        when(approvalConfiguration.getSegmentName()).thenReturn("seg1");
        when(approvalConfiguration.getStepName()).thenReturn("step1");
        when(keyPair.getKeyId()).thenReturn("accountKeyId");

        when(step.getName()).thenReturn("step1");
        when(layoutSegment.getName()).thenReturn("seg1");
        when(step.getAuthorizedKeyIds()).thenReturn(List.of("accountKeyId"));

        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalsForAccount(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), contains(restApprovalConfiguration));

        when(step.getName()).thenReturn("step1");
        when(layoutSegment.getName()).thenReturn("seg1");
        when(step.getAuthorizedKeyIds()).thenReturn(List.of("otherAccountKeyId"));
        assertThat(service.getApprovalsForAccount(SUPPLY_CHAIN_ID).getBody(), empty());

        when(step.getName()).thenReturn("step2");
        when(layoutSegment.getName()).thenReturn("seg1");
        assertThat(service.getApprovalsForAccount(SUPPLY_CHAIN_ID).getBody(), empty());


        when(layoutSegment.getName()).thenReturn("seg2");
        assertThat(service.getApprovalsForAccount(SUPPLY_CHAIN_ID).getBody(), empty());
    }

    @Test
    void getApprovalsForAccountNoLayout() {
        when(layoutMetaBlockRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.of(account));
        when(account.getActiveKeyPair()).thenReturn(keyPair);
        ResponseEntity<List<RestApprovalConfiguration>> responseEntity = service.getApprovalsForAccount(SUPPLY_CHAIN_ID);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), empty());

    }

    @Test
    void getApprovalsForAccountNoAccount() {
        when(accountSecurityContext.getAuthenticatedAccount()).thenReturn(Optional.empty());
        ArgosError exception = assertThrows(ArgosError.class, () -> service.getApprovalsForAccount(SUPPLY_CHAIN_ID));
        assertThat(exception.getMessage(), Is.is("not logged in"));
    }

    @Test
    void getReleaseConfiguration() {
        when(releaseConfigurationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.of(releaseConfiguration));
        when(configurationMapper.convertToRestReleaseConfiguration(releaseConfiguration)).thenReturn(restReleaseConfiguration);
        ResponseEntity<RestReleaseConfiguration> response = service.getReleaseConfiguration(SUPPLY_CHAIN_ID);
        assertThat(response.getBody(), is(restReleaseConfiguration));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    @Test
    void getReleaseConfigurationNotFound() {
        when(releaseConfigurationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.getReleaseConfiguration(SUPPLY_CHAIN_ID));
        assertThat(exception.getMessage(), is("404 NOT_FOUND \"release configuration not found\""));
    }

    @Test
    void createReleaseConfiguration() {
        when(configurationMapper.convertFromRestReleaseConfiguration(restReleaseConfiguration)).thenReturn(releaseConfiguration);
        ResponseEntity<RestReleaseConfiguration> response = service.createReleaseConfiguration(SUPPLY_CHAIN_ID, restReleaseConfiguration);
        verify(releaseConfigurationRepository).save(releaseConfiguration);
        verify(releaseConfiguration).setSupplyChainId(SUPPLY_CHAIN_ID);
        assertThat(response.getBody(), is(restReleaseConfiguration));
        assertThat(response.getStatusCodeValue(), is(200));
    }

    private static List<LayoutSegment> createSegmentAndStep() {
        return singletonList(LayoutSegment
                .builder()
                .name(SEGMENT_NAME)
                .steps(singletonList(Step.builder()
                        .name(STEP_NAME)
                        .build()))
                .build());
    }

}
