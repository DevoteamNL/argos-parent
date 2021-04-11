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

import com.argosnotary.argos.argos4j.FileCollector;
import com.argosnotary.argos.argos4j.LinkBuilder;
import com.argosnotary.argos.argos4j.LocalFileCollector;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;


/**
 * Jenkins recorder plugin to output signed link metadata for Jenkins pipeline
 * steps.
 */
public class ArgosWrapper extends SimpleBuildWrapper implements Serializable {

    /**
     * CredentialId for the key to load.
     * <p>
     * If not defined signing will not be performed.
     */
    @DataBoundSetter
    public String privateKeyCredentialId;

    /**
     * Name of the step to execute.
     */
    @DataBoundSetter
    public String stepName;

    /**
     * The host URL/URI where to post the argos metdata.
     * <p>
     * Protocol information *must* be included.
     */
    @DataBoundSetter
    public String supplyChainIdentifier;

    private LinkBuilder argosLinkBuilder;

    @DataBoundConstructor
    public ArgosWrapper(String privateKeyCredentialId, String stepName, String supplyChainIdentifier) {
        String credentialId = privateKeyCredentialId != null ? privateKeyCredentialId : ArgosServiceConfiguration.get().getPrivateKeyCredentialId();
        this.privateKeyCredentialId = credentialId;
        this.stepName = stepName;
        this.supplyChainIdentifier = supplyChainIdentifier;
    }

    @Override
    public void setUp(SimpleBuildWrapper.Context context,
                      Run<?, ?> build,
                      FilePath workspace,
                      Launcher launcher,
                      TaskListener listener,
                      EnvVars initialEnvironment) {

        listener.getLogger().println("[argos] wrapping step ");
        listener.getLogger().println("[argos] using step name: " + this.stepName);


        listener.getLogger().println("[argos] creating metadata... ");
        argosLinkBuilder = new ArgosJenkinsHelper(privateKeyCredentialId, stepName, supplyChainIdentifier).createArgosLinkBuilder();

        argosLinkBuilder.collectMaterials(createFileCollector(workspace));

        context.setDisposer(new PostWrap());
    }


    /**
     * Descriptor for {@link ArgosRecorder}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     * <p>
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    @Symbol("argosWrapper")
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        public DescriptorImpl() {
            super(ArgosWrapper.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return "argos record wrapper";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }

    private class PostWrap extends Disposer {

        @Override
        public void tearDown(Run<?, ?> build,
                             FilePath workspace,
                             Launcher launcher,
                             TaskListener listener) {
            argosLinkBuilder.collectProducts(createFileCollector(workspace));
            listener.getLogger().println("[argos] uploading metadata to: " + argosLinkBuilder.getSettings().getArgosServerBaseUrl());
            argosLinkBuilder.store(ArgosJenkinsHelper.getPrivateKeyPassword(privateKeyCredentialId));
        }
    }

    private FileCollector createFileCollector(FilePath workspace) {
        Path path = new File(workspace.getRemote()).toPath();
        return LocalFileCollector.builder()
                .basePath(path)
                .path(path)
                .build();
    }
}
