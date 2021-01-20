/**
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
import jenkins.model.*
import hudson.security.*
import hudson.tasks.*
import jenkins.branch.*
import jenkins.plugins.git.*
import groovy.json.JsonSlurper
import org.jenkinsci.plugins.workflow.multibranch.*
import com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger
import hudson.plugins.git.extensions.GitSCMExtension
import hudson.plugins.git.extensions.impl.PathRestriction
import hudson.plugins.git.extensions.impl.CloneOption
import hudson.plugins.git.extensions.impl.LocalBranch
import hudson.plugins.git.extensions.impl.CleanCheckout
import hudson.tasks.Shell
import hudson.plugins.git.GitSCM
import hudson.model.FreeStyleProject

import io.jenkins.plugins.argos.ArgosServiceConfiguration
import io.jenkins.plugins.argos.recorders.ArgosRecorder

import java.util.logging.Logger
import java.util.logging.Level

def Logger logger = Logger.getLogger("")

def instance = Jenkins.getInstance()

def job = instance.getJob("argos-test-app-pipeline")
    
if (job) {
    logger.info("--> project argos-test-app already defined, first delete it")
    job.delete();
}

job = instance.getJob("argos-test-app-freestyle-recording")
    
if (job) {
    logger.info("--> project argos-test-app already defined, first delete it")
    job.delete();
}
    
logger.info("--> set Argos Test App Project")
GitSCMSource scms = new GitSCMSource(null, "https://github.com/argosnotary/argos-test-app.git", "", "*", "", false)

extensions = [];

CloneOption ext1 = new CloneOption(false, false, "", 5) 
extensions.add(ext1)

LocalBranch ext2 = new LocalBranch("**")
extensions.add(ext2)

CleanCheckout ext3 = new CleanCheckout()
extensions.add(ext3)

scms.setExtensions(extensions);

WorkflowMultiBranchProject mp = instance.createProject(WorkflowMultiBranchProject.class, "argos-test-app-pipeline");
mp.getSourcesList().add(new BranchSource(scms, new DefaultBranchPropertyStrategy(null)));

argosConfig = instance.getExtensionList(ArgosServiceConfiguration)[0]

argosConfig.setArgosBaseUrl("http://argos-service:8080")
argosConfig.setPrivateKeyCredentialId("default-sa2")

FreeStyleProject fp = instance.createProject(FreeStyleProject.class, "argos-test-app-freestyle-recording")
fp.setScm(new GitSCM("https://github.com/argosnotary/argos-test-app.git"))
argosRecorder = new ArgosRecorder("root-label.child-label:argos-test-app", "default-sa2", "segment1", "build", '${BUILD_NUMBER}')
fp.getPublishersList().add(argosRecorder)
fp.getBuildersList().add(new Shell("mvn clean install"))

instance.save()
