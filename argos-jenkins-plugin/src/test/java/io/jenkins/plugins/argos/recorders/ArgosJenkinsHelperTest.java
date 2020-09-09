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
package io.jenkins.plugins.argos.recorders;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.LinkBuilder;

import io.jenkins.plugins.argos.ArgosServiceConfiguration;

public class ArgosJenkinsHelperTest {
    static String expectedUrl = "http://argos-url";
    static String expectedKeyId = "expectedKeyIdValue";

    @Rule
    public RestartableJenkinsRule jenkinsRule = new RestartableJenkinsRule();
    
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void createArgosLinkBuilderTest() {
        jenkinsRule.then(r -> {
            UsernamePasswordCredentialsImpl cred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                    "credentialId", "description", expectedKeyId, "admin");
            CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), cred);
            HtmlForm form = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlSelect credentialSelect = form.getSelectByName("_.privateKeyCredentialId");
            credentialSelect.getOption(1).click();
            r.submit(form);
            assertEquals("credentialId", ArgosServiceConfiguration.get().getPrivateKeyCredentialId());
            ArgosJenkinsHelper helper = new ArgosJenkinsHelper(
                    "credentialId", "layoutSegmentNameValue",
                    "stepNameValue", "foo.bar:supplyChainIdentifierValue", "runIdValue");
            LinkBuilder builder = helper.createArgosLinkBuilder();
            Argos4jSettings settings = builder.getSettings();
            assertEquals(expectedKeyId, settings.getKeyId());
            assertEquals(Arrays.asList("foo","bar"), settings.getPath());
            assertEquals("supplyChainIdentifierValue", settings.getSupplyChainName());
        });
    }

    @Test
    public void checkPropertiesTest() {
        ArgosJenkinsHelper helper = new ArgosJenkinsHelper("", "layoutSegmentNameValue",
                    "stepNameValue", "supplyChainIdentifierValue", "runIdValue");
        jenkinsRule.then(r -> {
            exceptionRule.expect(Argos4jError.class);
            exceptionRule.expectMessage("privateKeyCredentialId not configured");
            helper.createArgosLinkBuilder();
        });
        
    }

    @Test
    public void getCredentialsOkTest() throws Exception {
        jenkinsRule.then(r -> {
            UsernamePasswordCredentialsImpl cred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                    "credentialId", "description", expectedKeyId, "admin");
            CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), cred);
            HtmlForm form = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlSelect credentialSelect = form.getSelectByName("_.privateKeyCredentialId");
            credentialSelect.getOption(1).click();
            r.submit(form);
            assertEquals("credentialId", ArgosServiceConfiguration.get().getPrivateKeyCredentialId());

            StandardUsernamePasswordCredentials creds = ArgosJenkinsHelper.getCredentials("credentialId");
            assertEquals("credentialId", creds.getId());
            assertEquals(expectedKeyId, creds.getUsername());
            assertEquals("admin", creds.getPassword().getPlainText());


            exceptionRule.expect(Argos4jError.class);
            exceptionRule.expectMessage("Could not find credentials entry with ID 'foo'");
            ArgosJenkinsHelper.getCredentials("foo");

            char[] password = ArgosJenkinsHelper.getPrivateKeyPassword(expectedKeyId);
            assertEquals("admin".toCharArray(), password);
        });
    }

    @Test
    public void getArgosServiceBaseApiUrlTest() throws Exception {
        jenkinsRule.then(r -> {
            HtmlForm form = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput textbox = form.getInputByName("_.argosBaseUrl");
            textbox.setText(expectedUrl);
            r.submit(form);
            assertEquals(expectedUrl, ArgosServiceConfiguration.get().getArgosServiceBaseUrl());

            assertEquals(expectedUrl + "/api", ArgosJenkinsHelper.getArgosServiceBaseApiUrl());
        });

    }

}
