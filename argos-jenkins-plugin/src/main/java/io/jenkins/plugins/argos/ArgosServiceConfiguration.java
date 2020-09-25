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
package io.jenkins.plugins.argos;

import hudson.Extension;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Extension
public class ArgosServiceConfiguration extends GlobalConfiguration {

    /**
     * @return the singleton instance
     */
    public static ArgosServiceConfiguration get() {
        return GlobalConfiguration.all().get(ArgosServiceConfiguration.class);
    }

    private String argosBaseUrl;
    
    private String privateKeyCredentialId;

    @DataBoundConstructor
    public ArgosServiceConfiguration(String argosBaseUrl, String privateKeyCredentialId) {
        this.argosBaseUrl = argosBaseUrl;
        this.privateKeyCredentialId = privateKeyCredentialId;
    }

    public ArgosServiceConfiguration() {
        // When Jenkins is restarted, load any saved configuration from disk.
        load();
    }

    public String getArgosBaseUrl() {
        return argosBaseUrl;
    }

    public void setArgosBaseUrl(String argosBaseUrl) {
        this.argosBaseUrl = argosBaseUrl;
        save();
    }
    
    public String getPrivateKeyCredentialId() {
        return privateKeyCredentialId;
    }

    public void setPrivateKeyCredentialId(String privateKeyCredentialId) {
        this.privateKeyCredentialId = privateKeyCredentialId;
    }

    public FormValidation doCheckUrl(@QueryParameter String value) {
        if (StringUtils.isEmpty(value)) {
            return FormValidation.warning("Please specify a hostname.");
        }
        return FormValidation.ok();
    }
     

    public FormValidation doValidateConnection(@QueryParameter String url) {
        FormValidation formValidation;
        String inputUrl = url + "/actuator/health";
        try {
            URL conUrl = new URL(inputUrl);
            HttpURLConnection con = (HttpURLConnection) conUrl.openConnection();
            con.setRequestMethod("GET");
            if (con.getResponseCode() == 200) {
                formValidation = FormValidation.ok("Your In argos Service instance [%s] is alive!", conUrl);
            } else {
                formValidation = FormValidation.error("status code " + con.getResponseCode() + " on " + conUrl);
            }
            con.disconnect();
        } catch (IOException e) {
            formValidation = FormValidation.error("Error " + e.getMessage() + " on " + inputUrl);
        }
        return formValidation;
    }
    
    /**
     * populating the private key credentialId drop-down list
     */
    public ListBoxModel doFillPrivateKeyCredentialIdItems(@AncestorInPath Item item, @QueryParameter String privateKeyCredentialId) {

        StandardListBoxModel result = new StandardListBoxModel();
        if (item == null) {
            if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                return result.includeCurrentValue(privateKeyCredentialId);
            }
        } else {
            if (!item.hasPermission(Item.EXTENDED_READ)
                    && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                return result.includeCurrentValue(privateKeyCredentialId);
            }
        }
        return result
                .includeEmptyValue()
                .includeAs(ACL.SYSTEM,
                        Jenkins.get(),
                        StandardUsernamePasswordCredentials.class)
                .includeCurrentValue(privateKeyCredentialId);
    }

    public String getArgosServiceBaseUrl() {
        return this.argosBaseUrl;
    }
}
