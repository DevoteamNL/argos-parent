/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.argos4j.internal;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.VerificationResult;
import com.rabobank.argos.argos4j.internal.mapper.RestMapper;
import com.rabobank.argos.argos4j.rest.api.ApiClient;
import com.rabobank.argos.argos4j.rest.api.client.LinkApi;
import com.rabobank.argos.argos4j.rest.api.client.ServiceAccountApi;
import com.rabobank.argos.argos4j.rest.api.client.SupplychainApi;
import com.rabobank.argos.argos4j.rest.api.client.VerificationApi;
import com.rabobank.argos.argos4j.rest.api.model.RestArtifact;
import com.rabobank.argos.argos4j.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.argos4j.rest.api.model.RestServiceAccountKeyPair;
import com.rabobank.argos.argos4j.rest.api.model.RestVerifyCommand;
import com.rabobank.argos.domain.crypto.ServiceAccountKeyPair;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import feign.FeignException;
import org.mapstruct.factory.Mappers;

import java.util.List;


public class ArgosServiceClient {

    private final Argos4jSettings settings;
    private final ApiClient apiClient;

    public ArgosServiceClient(Argos4jSettings settings, char[] signingKeyPassphrase) {
        this.settings = settings;
        apiClient = new ApiClient("basicAuth").setBasePath(settings.getArgosServerBaseUrl());

        apiClient.setCredentials(settings.getSigningKeyId(), ServiceAccountKeyPair.calculateHashedPassphrase(settings.getSigningKeyId(), new String(signingKeyPassphrase)));
        apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void uploadLinkMetaBlockToService(LinkMetaBlock linkMetaBlock) {
        try {
            LinkApi linkApi = apiClient.buildClient(LinkApi.class);
            RestLinkMetaBlock restLinkMetaBlock = Mappers.getMapper(RestMapper.class).convertToRestLinkMetaBlock(linkMetaBlock);
            linkApi.createLink(getSupplyChainId(), restLinkMetaBlock);
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    public VerificationResult verify(List<Artifact> artifacts) {
        try {
            VerificationApi verificationApi = apiClient.buildClient(VerificationApi.class);
            List<RestArtifact> restArtifacts = Mappers.getMapper(RestMapper.class).convertToRestArtifacts(artifacts);
            return VerificationResult.builder().runIsValid(verificationApi.performVerification(getSupplyChainId(), new RestVerifyCommand().expectedProducts(restArtifacts)).getRunIsValid()).build();
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    public RestServiceAccountKeyPair getKeyPair() {
        try {
            ServiceAccountApi keyApi = apiClient.buildClient(ServiceAccountApi.class);
            return keyApi.getServiceAccountKey();
        } catch (FeignException e) {
            throw convertToArgos4jError(e);
        }
    }

    private Argos4jError convertToArgos4jError(FeignException e) {
        return new Argos4jError(e.getMessage(), e);
    }

    private String getSupplyChainId() {
        SupplychainApi supplychainApi = apiClient.buildClient(SupplychainApi.class);
        return supplychainApi.getSupplyChainByPath(settings.getSupplyChainName(), settings.getPath()).getId();
    }

}
