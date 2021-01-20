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

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.argosnotary.argos.argos4j.Argos4j;
import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.LinkBuilder;
import com.argosnotary.argos.argos4j.LinkBuilderSettings;
import com.argosnotary.argos.domain.SupplyChainHelper;

import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.security.ACL;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.model.Jenkins;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class ArgosJenkinsHelper {

    private final String privateKeyCredentialId;
    private final String layoutSegmentName;
    private final String stepName;
    private final String supplyChainIdentifier;
    private final String runId;

    public LinkBuilder createArgosLinkBuilder() {

        String version = Optional.ofNullable(Jenkins.getInstanceOrNull())
                .flatMap(jenkins -> Optional.ofNullable(jenkins.getPlugin("bouncycastle-api")))
                .map(Plugin::getWrapper)
                .map(PluginWrapper::getVersion).orElseThrow(() -> new Argos4jError("bouncycastle-api plugin not installed"));

        if (Float.parseFloat(version) < 1.8F) {
            throw new Argos4jError("bouncycastle-api plugin " + version + " installed, minimal version 1.8 required");
        }
        
        checkProperty(privateKeyCredentialId, "privateKeyCredentialId");
        checkProperty(layoutSegmentName, "layoutSegmentName");
        checkProperty(stepName, "stepName");
        checkProperty(supplyChainIdentifier, "supplyChainIdentifier");
        checkProperty(runId, "runId");


        String argosServiceBaseUrl = getArgosServiceBaseApiUrl();
        checkProperty(argosServiceBaseUrl, "argosServiceBaseUrl");
        log.info("argos4j version = {}", Argos4j.getVersion());
        log.info("argosServiceBaseUrl = {}", argosServiceBaseUrl);

        String supplyChainName = SupplyChainHelper.getSupplyChainName(supplyChainIdentifier);
        List<String> path = SupplyChainHelper.getSupplyChainPath(supplyChainIdentifier);

        return new Argos4j(
                Argos4jSettings.builder()
                    .path(path)
                    .argosServerBaseUrl(argosServiceBaseUrl)
                    .keyId(getCredentials(privateKeyCredentialId).getUsername())
                    .supplyChainName(supplyChainName).build())
                    .getLinkBuilder(
                            LinkBuilderSettings.builder()
                                .layoutSegmentName(layoutSegmentName)
                                .stepName(stepName)
                                .runId(runId).build());
    }

    public static char[] getPrivateKeyPassword(String privateKeyCredentialId) {
        return getCredentials(privateKeyCredentialId).getPassword().getPlainText().toCharArray();
    }

    private void checkProperty(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new Argos4jError(fieldName + " not configured");
        }
    }

    public static StandardUsernamePasswordCredentials getCredentials(String privateKeyCredentialId) {
        log.info("credential id = {}", privateKeyCredentialId);
        StandardUsernamePasswordCredentials fileCredential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()
                ),
                CredentialsMatchers.withId(privateKeyCredentialId)
        );

        if (fileCredential == null)
            throw new Argos4jError(" Could not find credentials entry with ID '" + privateKeyCredentialId + "' ");

        return fileCredential;
    }
    
    public static String getArgosServiceBaseApiUrl() {
        return ArgosServiceConfiguration.get().getArgosServiceBaseUrl() + "/api";
    }

}
