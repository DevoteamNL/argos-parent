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
package io.jenkins.plugins.argos;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.collect.ImmutableSet;
import com.argosnotary.argos.argos4j.Argos4j;
import com.argosnotary.argos.argos4j.Argos4jSettings;
import com.argosnotary.argos.argos4j.ReleaseBuilder;
import com.argosnotary.argos.domain.release.ReleaseResult;

import org.jenkinsci.plugins.workflow.graph.FlowNode;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.argos.recorders.ArgosJenkinsHelper;


public class ArgosReleaseStep extends Step implements Serializable {
    
    private static final long serialVersionUID = -892766264355302985L;
    private String credentialId;
    private final String argosSettingsFile;
    private Map<String,Map<String,String>> releaseConfigMap;

    @DataBoundConstructor
    public ArgosReleaseStep(String argosSettingsFile) {
        super();
        this.credentialId = ArgosServiceConfiguration.get().getPrivateKeyCredentialId();
        this.argosSettingsFile = argosSettingsFile;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new ArgosReleaseStepExecution(this, context);
    }
    
    public String getCredentialId() {
        return credentialId;
    }

    @DataBoundSetter
    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }
    
    public Map<String, Map<String, String>> getReleaseConfigMap() {
        return releaseConfigMap;
    }

    @DataBoundSetter
    public void setReleaseConfigMap(Map<String, Map<String, String>> releaseConfigMap) {
        this.releaseConfigMap = releaseConfigMap;
    }

    public String getArgosSettingsFile() {
        return argosSettingsFile;
    }
    
    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "argosRelease";
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "Release Deployables on Argos Notary";
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, FlowNode.class, TaskListener.class, Launcher.class);
        }
    }
    
    private static class ArgosReleaseStepExecution extends SynchronousStepExecution<Boolean> {
        private static final long serialVersionUID = 3790704651797090531L;

        private ArgosReleaseStep step;
        
        protected ArgosReleaseStepExecution(ArgosReleaseStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Boolean run() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);
            StandardUsernamePasswordCredentials credentials = ArgosJenkinsHelper.getCredentials(step.getCredentialId());
            Argos4jSettings settings = Argos4jSettings.readSettings(Paths.get(step.getArgosSettingsFile()));
            settings.setArgosServerBaseUrl(ArgosJenkinsHelper.getArgosServiceBaseApiUrl());
            settings.setKeyId(credentials.getUsername());
            settings.setKeyPassphrase(credentials.getPassword().getPlainText());
            settings.enrichReleaseCollectors(step.getReleaseConfigMap());
            Argos4j argos4j = new Argos4j(settings);
            ReleaseBuilder releaseBuilder = argos4j.getReleaseBuilder();
            settings.getReleaseCollectors().forEach(r -> releaseBuilder.addFileCollector(r.getCollector()));
            ReleaseResult result = releaseBuilder.release(credentials.getPassword().getPlainText().toCharArray());
            listener.getLogger().println(String.format("[argos] release valid: [%s] ", result.isReleaseIsValid()));
            return result.isReleaseIsValid();
        }
        
    }
    
    
    

}
