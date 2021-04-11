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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.argosnotary.argos.argos4j.Argos4jError;
import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.LinkBuilder;
import com.argosnotary.argos.argos4j.LocalFileCollector;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 */
public class ArgosRecorder extends Recorder {

    @DataBoundSetter
    private String supplyChainIdentifier;
    /**
     * Credential id with private key to load.
     * <p>
     * If not defined signing will not be performed.
     */
    @DataBoundSetter
    private String privateKeyCredentialId;

    /**
     * Name of the step to execute.
     */
    @DataBoundSetter
    private String stepName;

    /**
     * Link metadata used to record this step
     */
    private LinkBuilder argos4jLinkBuilder;


    @DataBoundConstructor
    public ArgosRecorder(String supplyChainIdentifier, String privateKeyCredentialId, String stepName) {
        this.stepName = stepName;
        this.supplyChainIdentifier = supplyChainIdentifier;
        String credentialId = privateKeyCredentialId != null ? privateKeyCredentialId : ArgosServiceConfiguration.get().getPrivateKeyCredentialId();
        this.privateKeyCredentialId = credentialId;
    }

    public String getSupplyChainIdentifier() {
        return supplyChainIdentifier;
    }

    public String getPrivateKeyCredentialId() {
        return privateKeyCredentialId;
    }

    public String getStepName() {
        return stepName;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        try {
            String cwdStr = getCwdStr(build);
            listener.getLogger().println("[argos] Recording state before build " + cwdStr);
            listener.getLogger().println("[argos] using step name: " + stepName);

            EnvVars environment = build.getEnvironment(listener);
            argos4jLinkBuilder = new ArgosJenkinsHelper(
                    environment.expand(privateKeyCredentialId),
                    environment.expand(stepName),
                    environment.expand(supplyChainIdentifier)).createArgosLinkBuilder();

            argos4jLinkBuilder.collectMaterials(createFileCollector(cwdStr));
            return true;
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private FileCollector createFileCollector(String cwdStr) {
        Path path = new File(cwdStr).toPath();
        return LocalFileCollector.builder()
                .path(path)
                .basePath(path)
                .build();
    }

    private String getCwdStr(AbstractBuild<?, ?> build) {
        return Optional.ofNullable(build.getWorkspace()).map(FilePath::getRemote).orElseThrow(() -> new Argos4jError("[argos] Cannot get the build workspace"));
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {

        listener.getLogger().println("[argos] Recording state after build ");

        argos4jLinkBuilder.collectProducts(createFileCollector(getCwdStr(build)));
        listener.getLogger().println("[argos] Dumping metadata to: " + argos4jLinkBuilder.getSettings().getArgosServerBaseUrl());

        argos4jLinkBuilder.store(ArgosJenkinsHelper.getPrivateKeyPassword(privateKeyCredentialId));

        return true;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @SuppressWarnings("rawtypes")
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        @Override
        public String getDisplayName() {
            return "argos provenance plugin";
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

        /**
         * validating the credentialId
         */
        public FormValidation doCheckprivateKeyCredentialId(@AncestorInPath Item item, @QueryParameter String value) {
            if (item == null) {
                if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok();
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ) && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok();
                }
            }
            if (StringUtils.isBlank(value)) {
                return FormValidation.ok();
            }
            return FormValidation.ok();
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}
