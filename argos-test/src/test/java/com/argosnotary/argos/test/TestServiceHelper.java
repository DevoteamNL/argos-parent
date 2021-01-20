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
package com.argosnotary.argos.test;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.argosnotary.argos.argos4j.rest.api.client.PersonalAccountApi;
import com.argosnotary.argos.argos4j.rest.api.client.ServiceAccountApi;
import com.argosnotary.argos.argos4j.rest.api.model.RestKeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.RestLabel;
import com.argosnotary.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.argos4j.rest.api.model.RestPersonalAccount;
import com.argosnotary.argos.argos4j.rest.api.model.RestServiceAccount;
import com.argosnotary.argos.argos4j.rest.api.model.RestServiceAccountKeyPair;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.crypto.ServiceAccountKeyPair;
import com.argosnotary.argos.test.rest.api.ApiClient;
import com.argosnotary.argos.test.rest.api.client.IntegrationTestServiceApi;
import com.argosnotary.argos.test.rest.api.model.TestLayoutMetaBlock;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.util.List;

import static com.argosnotary.argos.argos4j.rest.api.model.RestPermission.LINK_ADD;
import static com.argosnotary.argos.argos4j.rest.api.model.RestPermission.READ;
import static com.argosnotary.argos.argos4j.rest.api.model.RestPermission.TREE_EDIT;
import static com.argosnotary.argos.test.ServiceStatusHelper.getHierarchyApi;
import static com.argosnotary.argos.test.ServiceStatusHelper.getLayoutApi;
import static com.argosnotary.argos.test.ServiceStatusHelper.getPersonalAccountApi;
import static com.argosnotary.argos.test.ServiceStatusHelper.getServiceAccountApi;
import static com.argosnotary.argos.test.ServiceStatusHelper.getToken;

class TestServiceHelper {

    private static final String DEFAULT_USER_2 = "Default User2";
    private static final String DEFAULT_USER1 = "Default User";
    private static Properties properties = Properties.getInstance();

    static void clearDatabase() {
        getTestApi().resetDatabase();
    }

    private static IntegrationTestServiceApi getTestApi() {
        return getApiClient().buildClient(IntegrationTestServiceApi.class);
    }

    private static ApiClient getApiClient() {
        return new ApiClient().setBasePath(properties.getIntegrationTestServiceBaseUrl() + "/integration-test");
    }

    static DefaultTestData createDefaultTestData() {
        getTestApi().resetDatabaseAll();
        DefaultTestData defaultTestData = new DefaultTestData();
        defaultTestData.setAdminToken(getToken("Luke Skywalker", "Skywalker", "luke@skywalker.imp"));
        createDefaultRootLabel(defaultTestData);
        createDefaultPersonalAccount(defaultTestData);
        createDefaultSaAccounts(defaultTestData);
        return defaultTestData;
    }

    private static void createDefaultRootLabel(DefaultTestData hierarchy) {
        hierarchy.setDefaultRootLabel(getHierarchyApi(hierarchy.getAdminToken()).createLabel(new RestLabel().name("default-root-label")));
    }

    private static void createDefaultPersonalAccount(DefaultTestData defaultTestData) {
        String defaultUser1Token = getToken(DEFAULT_USER1, "User", "default@nl.nl");
        PersonalAccountApi personalAccountApi = getPersonalAccountApi(defaultTestData.getAdminToken());
        RestPersonalAccount defaultUser1 = personalAccountApi.searchPersonalAccounts(null, DEFAULT_USER1, null, null).iterator().next();
        personalAccountApi.updateLocalPermissionsForLabel(defaultUser1.getId(), defaultTestData.getDefaultRootLabel().getId(), List.of(READ, TREE_EDIT, LINK_ADD));

        TestDateKeyPair keyPair = readKeyPair(1);
        getPersonalAccountApi(defaultUser1Token).createKey(new RestKeyPair()
                .encryptedPrivateKey(keyPair.getEncryptedPrivateKey())
                .publicKey(keyPair.getPublicKey())
                .keyId(keyPair.getKeyId()));

        defaultTestData.getPersonalAccounts().put("default-pa1", DefaultTestData.PersonalAccount.builder()
                .passphrase(keyPair.getPassphrase())
                .keyId(keyPair.getKeyId())
                .accountId(defaultUser1.getId())
                .token(defaultUser1Token)
                .publicKey(keyPair.getPublicKey())
                .build());

        String defaultUser2Token = getToken(DEFAULT_USER_2, "User2", "default2@nl.nl");
        RestPersonalAccount defaultUser2 = personalAccountApi.searchPersonalAccounts(null, DEFAULT_USER_2, null, null).iterator().next();
        defaultTestData.getPersonalAccounts().put("default-pa2", DefaultTestData.PersonalAccount.builder()
                .accountId(defaultUser2.getId())
                .token(defaultUser2Token)
                .build());
    }

    private static void createDefaultSaAccounts(DefaultTestData defaultTestData) {
        createSaWithActiveKey(defaultTestData, readKeyPair(1), "default-sa1");
        createSaWithActiveKey(defaultTestData, readKeyPair(2), "default-sa2");
        createSaWithActiveKey(defaultTestData, readKeyPair(3), "default-sa3");
        createSaWithActiveKey(defaultTestData, readKeyPair(4), "default-sa4");
        createSaWithActiveKey(defaultTestData, readKeyPair(5), "default-sa5");
    }

    private static void createSaWithActiveKey(DefaultTestData defaultTestData, TestDateKeyPair keyPair, String name) {
        ServiceAccountApi serviceAccountApi = getServiceAccountApi(defaultTestData.getPersonalAccounts().get("default-pa1").getToken());
        RestServiceAccount sa = serviceAccountApi.createServiceAccount(new RestServiceAccount().parentLabelId(defaultTestData.getDefaultRootLabel().getId()).name(name));

        String hashedKeyPassphrase = ServiceAccountKeyPair.calculateHashedPassphrase(keyPair.getKeyId(), keyPair.getPassphrase());

        serviceAccountApi.createServiceAccountKeyById(sa.getId(),
                new RestServiceAccountKeyPair()
                		.keyId(keyPair.getKeyId())
                        .hashedKeyPassphrase(hashedKeyPassphrase)
                        .encryptedPrivateKey(keyPair.getEncryptedPrivateKey())
                        .publicKey(keyPair.getPublicKey()));
        defaultTestData.getServiceAccount().put(name,
                DefaultTestData.ServiceAccount.builder()
                        .passphrase(keyPair.getPassphrase())
                        .keyId(keyPair.getKeyId())
                        .hashedKeyPassphrase(hashedKeyPassphrase)
                        .publicKey(keyPair.getPublicKey())
                        .build());
    }

    private static TestDateKeyPair readKeyPair(int index) {
        try {
            return new ObjectMapper().readValue(TestServiceHelper.class.getResourceAsStream("/testmessages/key/default-test-keypair" + index + ".json"), TestDateKeyPair.class);
        } catch (IOException e) {
            throw new ArgosError(e.getMessage(), e);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class TestDateKeyPair {
        private String keyId;
        private byte[] publicKey;
        private String passphrase;
        private byte[] encryptedPrivateKey;
    }

    static void signAndStoreLayout(String token, String supplyChainId, RestLayoutMetaBlock restLayout, String keyId, String password) {
        RestMapper mapper = Mappers.getMapper(RestMapper.class);
        TestLayoutMetaBlock testLayout = mapper.mapRestLayout(restLayout);
        TestLayoutMetaBlock signed = getTestApi().signLayout(password, keyId, testLayout);
        getLayoutApi(token).createOrUpdateLayout(supplyChainId, mapper.mapTestLayout(signed));
    }

}
