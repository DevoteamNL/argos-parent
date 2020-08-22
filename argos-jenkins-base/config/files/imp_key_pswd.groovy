/**
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
import jenkins.model.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.CredentialsScope
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import com.cloudbees.plugins.credentials.SecretBytes
import com.cloudbees.plugins.credentials.impl.*
import groovy.json.JsonSlurper
import java.util.logging.Logger
import java.util.logging.Level
import java.nio.file.Files

def Logger logger = Logger.getLogger("")

store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

secretKeySa2 = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "default-sa2",
    "default-sa2",
    "6a58de92fc02d7835faba93ee26d91d7e53f03eb8f86e2518632c34596c5ef3f",
    "test")
    
domain = Domain.global()
    
// Add credentials to Jenkins credential store
store.addCredentials(domain, secretKeySa2)

logger.info("--> credetials added.")
