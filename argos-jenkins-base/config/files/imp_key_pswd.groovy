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
