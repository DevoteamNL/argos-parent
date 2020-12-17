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
package io.jenkins.plugins.argos;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import hudson.util.FormValidation;
public class ArgosServiceConfigurationTest {

    @Rule
    public RestartableJenkinsRule rr = new RestartableJenkinsRule();
    
    @Rule
    public WireMockRule wireMockServer;

    @Test
    public void configurationTest() throws IOException {

        String expectedUrl = "http://argos-url";
        String expectedKeyId = "expectedKeyIdValue";
        rr.then(r -> {
            UsernamePasswordCredentialsImpl cred = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL,
                    "credentialId", "description", expectedKeyId, "admin");
            CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), cred);
            HtmlForm form = r.createWebClient().goTo("configure").getFormByName("config");
            HtmlTextInput textbox = form.getInputByName("_.argosBaseUrl");
            textbox.setText(expectedUrl);
            HtmlSelect credentialSelect = form.getSelectByName("_.privateKeyCredentialId");
            credentialSelect.getOption(1).click();
            r.submit(form);
            assertEquals(expectedUrl, ArgosServiceConfiguration.get().getArgosServiceBaseUrl());
            assertEquals("credentialId", ArgosServiceConfiguration.get().getPrivateKeyCredentialId());
        });
    }
    
    @Test
    public void doCheckUrlTest() {
        ArgosServiceConfiguration config = new ArgosServiceConfiguration("url", "privateKeyCredentialId");
        assertEquals(FormValidation.Kind.OK, config.doCheckUrl("url").kind);
        assertEquals(FormValidation.Kind.WARNING, config.doCheckUrl(null).kind);
        assertEquals("Please specify a hostname.", config.doCheckUrl(null).getMessage());
    }
    
    @Test
    public void doValidateConnectionOkTest() {
        ArgosServiceConfiguration config = new ArgosServiceConfiguration("url", "privateKeyCredentialId");
        String url = "http://localhost:2500";
        FormValidation validation = config.doValidateConnection(url);
        assertEquals(FormValidation.Kind.ERROR, validation.kind);
        assertEquals("Error Connection refused (Connection refused) on http://localhost:2500/actuator/health", validation.getMessage());
        
        wireMockServer = new WireMockRule(options().port(2500), false);

        wireMockServer.start();
        wireMockServer.stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(ok()));
        validation = config.doValidateConnection(url);
        assertEquals(FormValidation.Kind.OK, validation.kind);
        assertEquals("Your In argos Service instance [http://localhost:2500/actuator/health] is alive!", validation.getMessage());
        wireMockServer.stop();
    }
    
    @Test
    public void doValidateConnectionNOKTest() {
        ArgosServiceConfiguration config = new ArgosServiceConfiguration("url", "privateKeyCredentialId");
        String url = "http://localhost:2500";
        
        wireMockServer = new WireMockRule(options().port(2500), false);

        wireMockServer.start();
        wireMockServer.stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(badRequest()));
        FormValidation validation = config.doValidateConnection(url);
        assertEquals(FormValidation.Kind.ERROR, validation.kind);
        assertEquals("status code 400 on http://localhost:2500/actuator/health", validation.getMessage());
        wireMockServer.stop();
    }

}
