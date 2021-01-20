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
import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.LinkBuilder;

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
                    "credentialId", "layout-segment-name-value",
                    "stepname-value", "foo.bar:supplychain-identifier-value", "runIdValue");
            LinkBuilder builder = helper.createArgosLinkBuilder();
            Argos4jSettings settings = builder.getSettings();
            assertEquals(expectedKeyId, settings.getKeyId());
            assertEquals(Arrays.asList("foo","bar"), settings.getPath());
            assertEquals("supplychain-identifier-value", settings.getSupplyChainName());
        });
    }

    @Test
    public void checkPropertiesTest() {
        ArgosJenkinsHelper helper = new ArgosJenkinsHelper("", "layout-segment-name-value",
                    "stepname-value", "supplychain-identifier-value", "runIdValue");
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
