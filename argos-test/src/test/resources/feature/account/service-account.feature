#
# Argos Notary - A new way to secure the Software Supply Chain
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
# Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#

Feature: Non Personal Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.personalAccounts['default-pa1']
    * def rootLabelId = defaultTestData.defaultRootLabel.id;
    * configure headers = call read('classpath:headers.js') { token: #(keyPair.token)}    
    * def keyPair2 = defaultTestData.personalAccounts['default-pa2']


  Scenario: store a service account with valid name should return a 201 and commit to audit log
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * match result.response == { name: 'sa1', id: '#uuid', parentLabelId: '#uuid' }

    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createServiceAccount'
    And match stringResponse contains 'serviceAccount'


  Scenario: delete service account should return a 200 and get should return a 403 and commit to audit log
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method DELETE
    Then status 204
    Given path restPath
    When method GET
    Then status 403
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'deleteServiceAccount'
    And match stringResponse contains 'serviceAccountId'

  Scenario: delete service account without TREE_EDIT permission should return a 403 error
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    * configure headers = call read('classpath:headers.js') { token: #(keyPair2.token)}
    Given path restPath
    When method DELETE
    Then status 403

  Scenario: store a service account without TREE_EDIT permission should return a 403 error
    * configure headers = call read('classpath:headers.js') { token: #(keyPair2.token)}
    Given path '/api/serviceaccount'
    And request { name: 'sa1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 403

  Scenario: store a service account without authorization should return a 401 error
    * configure headers = null
    Given path '/api/serviceaccount'
    And request { name: 'sa1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 401

  Scenario: store a service account with a non existing parent label id should return a 403
    Given path '/api/serviceaccount'
    And request { name: 'label', parentLabelId: '940935f6-22bc-4d65-8c5b-a0599dedb510'}
    When method POST
    Then status 403
    And match response.message == 'Access denied'

  Scenario: store two service accounts with the same name should return a 400
    * call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    Given path '/api/serviceaccount'
    And request { name: 'sa1', parentLabelId: #(rootLabelId)}
    When method POST
    Then status 400
    And match response.messages[0].message contains "service account with name: sa1 and parentLabelId:"

  Scenario: retrieve service account should return a 200
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'sa1', id: '#(result.response.id)', parentLabelId: #(rootLabelId)}

  Scenario: retrieve service account without READ permission should return a 403 error
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountWithNoReadPermission = call read('classpath:feature/account/create-personal-account.feature') {name: 'unauthorized person',email: 'local.unauthorized@extra.nogo'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermission.response.token)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 403

  Scenario: retrieve service account with implicit READ permission should return a 200 error
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["TREE_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(info.labelId)}
    * def restPath = '/api/serviceaccount/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'sa1', id: '#(result.response.id)', parentLabelId: #(info.labelId)}

  Scenario: update a service account should return a 200 and commit to audit log
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def restPath = '/api/serviceaccount/'+accountId
    Given path restPath
    And request { name: 'sa2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 200
    And match response == { name: 'sa2', id: '#(accountId)', parentLabelId: #(rootLabelId)}
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'updateServiceAccountById'
    And match stringResponse contains 'serviceAccountId'
    And match stringResponse contains 'serviceAccount'

  Scenario: update a service account without TREE_EDIT permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * configure headers = call read('classpath:headers.js') { token: #(keyPair2.token)}
    * def accountId = createResult.response.id
    * def restPath = '/api/serviceaccount/'+accountId
    Given path restPath
    And request { name: 'sa2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 403

  Scenario: create a service account key should return a 200 and commit to audit log
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * def result = call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * match result.response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createServiceAccountKeyById'
    And match stringResponse contains 'serviceAccountId'
    And match stringResponse contains 'keyPair'

  Scenario: create a service account key without TREE_EDIT permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = call read('classpath:headers.js') { token: #(keyPair2.token)}
    Given path '/api/serviceaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 403

  Scenario: create a service account key without authorization should return a 401 error
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = null
    Given path '/api/serviceaccount/'+accountId+'/key'
    And request keyPair
    When method POST
    Then status 401

  Scenario: get a active service account key should return a 200
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key with implicit read permission should return a 200

    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["TREE_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def result = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(info.labelId)}
    * def accountId = result.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+result.response.id+'/key'
    Given path restPath
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get a active service account key without READ permission should return a 403 error
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["ASSIGN_ROLE"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    Given path restPath
    When method GET
    Then status 403

  Scenario: get a active service account key without authorization should return a 401 error
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPath = '/api/serviceaccount/'+accountId+'/key'
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: get active key of authenticated sa should return a 200
    * def keypairResponse = call read('classpath:feature/account/create-service-account-with-key.feature') {accountName: 'sa1', parentLabelId: #(rootLabelId), keyFile: 'sa-keypair1'}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers =  call read('classpath:headers.js') { username: #(keyPair.keyId),password:#(keyPair.hashedKeyPassphrase)}
    Given path '/api/serviceaccount/me/activekey'
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

  Scenario: get active key of authenticated sa with invalid credentials should return a 401
    * def keypairResponse = call read('classpath:feature/account/create-service-account-with-key.feature') {accountName: 'sa1', parentLabelId: #(rootLabelId), keyFile: 'sa-keypair1'}
    * def keyPair = keypairResponse.response
    * configure headers =  call read('classpath:headers.js') { username: fake,password:fake}
    Given path '/api/serviceaccount/me/activekey'
    When method GET
    Then status 401

  Scenario: get an active service account key after update should return a 200
    * def createResult = call read('create-service-account.feature') { name: 'sa1', parentLabelId: #(rootLabelId)}
    * def accountId = createResult.response.id
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * call read('create-service-account-key.feature') {accountId: #(accountId), key: #(keyPair)}
    * def restPathKey = '/api/serviceaccount/'+accountId+'/key'
    * def restPathUpdate = '/api/serviceaccount/'+ accountId
    Given path restPathUpdate
    And request { name: 'sa2', parentLabelId: #(rootLabelId)}
    When method PUT
    Then status 200
    Given path restPathKey
    When method GET
    Then status 200
    And match response == {keyId: #(keyPair.keyId), publicKey: #(keyPair.publicKey), encryptedPrivateKey: #(keyPair.encryptedPrivateKey)}

