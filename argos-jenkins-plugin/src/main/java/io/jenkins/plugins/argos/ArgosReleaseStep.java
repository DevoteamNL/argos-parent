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
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.ReleaseBuilder;
import com.rabobank.argos.domain.release.ReleaseResult;

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
