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
package com.argosnotary.argos.test;

import com.argosnotary.argos.argos4j.Argos4j;
import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.LinkBuilder;
import com.argosnotary.argos.argos4j.LinkBuilderSettings;
import com.argosnotary.argos.argos4j.LocalFileCollector;
import com.argosnotary.argos.argos4j.ReleaseBuilder;
import com.argosnotary.argos.argos4j.VerifyBuilder;
import com.argosnotary.argos.argos4j.rest.api.model.RestLabel;
import com.argosnotary.argos.argos4j.rest.api.model.RestLayout;
import com.argosnotary.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.argos4j.rest.api.model.RestLayoutSegment;
import com.argosnotary.argos.argos4j.rest.api.model.RestMatchRule;
import com.argosnotary.argos.argos4j.rest.api.model.RestPublicKey;
import com.argosnotary.argos.argos4j.rest.api.model.RestRule;
import com.argosnotary.argos.argos4j.rest.api.model.RestStep;
import com.argosnotary.argos.argos4j.rest.api.model.RestSupplyChain;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static com.argosnotary.argos.test.ServiceStatusHelper.getHierarchyApi;
import static com.argosnotary.argos.test.ServiceStatusHelper.getSupplychainApi;
import static com.argosnotary.argos.test.ServiceStatusHelper.waitForArgosServiceToStart;
import static com.argosnotary.argos.test.TestServiceHelper.clearDatabase;
import static com.argosnotary.argos.test.TestServiceHelper.createDefaultTestData;
import static com.argosnotary.argos.test.TestServiceHelper.signAndStoreLayout;
import static org.hamcrest.MatcherAssert.assertThat;

class Argos4jIT {

    private static Properties properties = Properties.getInstance();
    private DefaultTestData.ServiceAccount serviceAccount;
    private DefaultTestData.PersonalAccount personalAccount;

    @BeforeAll
    static void setUp() {
        waitForArgosServiceToStart();
    }

    @BeforeEach
    void reset() {
        clearDatabase();
    }

    @Test
    void postLinkMetaBlockWithSignatureReleaseAndVerify() {

        DefaultTestData defaultTestData = createDefaultTestData();
        String adminAccountToken = defaultTestData.getAdminToken();
        getHierarchyApi(adminAccountToken).updateLabelById(defaultTestData.getDefaultRootLabel().getId(), new RestLabel().name("root-label"));
        RestLabel childLabel = getHierarchyApi(adminAccountToken).createLabel(new RestLabel().name("child-label").parentLabelId(defaultTestData.getDefaultRootLabel().getId()));

        String supplyChainId = getSupplychainApi(adminAccountToken).createSupplyChain(new RestSupplyChain().name("test-supply-chain").parentLabelId(childLabel.getId())).getId();

        serviceAccount = defaultTestData.getServiceAccount().values().iterator().next();

        personalAccount = defaultTestData.getPersonalAccounts().values().iterator().next();

        RestLayoutMetaBlock layout = new RestLayoutMetaBlock().layout(createLayout());

        signAndStoreLayout(personalAccount.getToken(), supplyChainId, layout, personalAccount.getKeyId(), personalAccount.getPassphrase());
        Argos4jSettings settings = Argos4jSettings.builder()
                .argosServerBaseUrl(properties.getApiBaseUrl() + "/api")
                .supplyChainName("test-supply-chain")
                .path(List.of("root-label", "child-label"))
                .keyId(serviceAccount.getKeyId())
                .build();
        Argos4j argos4j = new Argos4j(settings);
        LinkBuilder linkBuilder = argos4j.getLinkBuilder(LinkBuilderSettings.builder().layoutSegmentName("layoutsegmentname").stepName("build").runId("runId").build());
        FileCollector fileCollector = LocalFileCollector.builder().path(new File(".").toPath()).basePath(new File(".").toPath()).build();
        linkBuilder.collectProducts(fileCollector);
        linkBuilder.collectMaterials(fileCollector);
        linkBuilder.store(serviceAccount.getPassphrase().toCharArray());

        File fileToVerify = new File("src/test/resources/karate-config.js");
        
        FileCollector collector = LocalFileCollector.builder()
                .path(fileToVerify.toPath())
                .basePath(fileToVerify.toPath().getParent()).build();
        
        ReleaseBuilder releaseBuilder = argos4j.getReleaseBuilder();
        
        releaseBuilder.addFileCollector(collector).release(serviceAccount.getPassphrase().toCharArray());


        VerifyBuilder verifyBuilder = argos4j.getVerifyBuilder();

        boolean runIsValid = verifyBuilder.addFileCollector(collector)
                .verify().isRunIsValid();

        assertThat(runIsValid, Matchers.is(true));
    }

    private RestLayout createLayout() {
        return new RestLayout()
                .addKeysItem(new RestPublicKey()
                		.keyId(personalAccount.getKeyId())
                		.publicKey(personalAccount.getPublicKey()))
                .addKeysItem(new RestPublicKey()
                		.keyId(serviceAccount.getKeyId())
                		.publicKey(serviceAccount.getPublicKey()))
                .addAuthorizedKeyIdsItem(personalAccount.getKeyId())
                .addExpectedEndProductsItem(new RestMatchRule()
                        .destinationSegmentName("layoutsegmentname")
                        .destinationStepName("build")
                        .destinationType(RestMatchRule.DestinationTypeEnum.PRODUCTS)
                        .destinationPathPrefix("src/test/resources/")
                        .pattern("karate-config.js"))
                .addLayoutSegmentsItem(new RestLayoutSegment().name("layoutsegmentname")
                        .addStepsItem(new RestStep().requiredNumberOfLinks(1)
                                .addAuthorizedKeyIdsItem(serviceAccount.getKeyId())
                                .addExpectedProductsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .addExpectedMaterialsItem(new RestRule().ruleType(RestRule.RuleTypeEnum.ALLOW).pattern("**"))
                                .name("build")));
    }
}
