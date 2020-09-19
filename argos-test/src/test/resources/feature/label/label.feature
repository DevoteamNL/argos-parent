#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

Feature: Label

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}

  Scenario: store a root label with valid name should return a 201 and commit to auditlog
    * def result = call read('create-label.feature') { name: 'label1'}
    * match result.response == { name: 'label1', id: '#uuid' }
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createLabel'
    And match stringResponse contains 'label'

  Scenario: store a root label without TREE_EDIT permission name should return a 403
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path '/api/label'
    And request { name: 'label1'}
    When method POST
    Then status 403

  Scenario: store a root label with invalid name should return a 400
    Given path '/api/label'
    And request { name: '1label'}
    When method POST
    Then status 400
    And match response.messages[0].message == 'must match "^([a-z]|[a-z][a-z0-9-]*[a-z0-9])?$"'

  Scenario: store a label without authorization should return a 401 error
    * configure headers = null
    Given path '/api/label'
    And request { name: 'label1'}
    When method POST
    Then status 401

  Scenario: store two root labels with the same name should return a 400
    * def result = call read('create-label.feature') { name: 'label1'}
    * match result.response == { name: 'label1', id: '#uuid' }
    Given path '/api/label'
    And request { name: 'label1'}
    When method POST
    Then status 400
    And match response.messages[0].message == 'label with name: label1 and parentLabelId: null already exists'


  Scenario: retrieve root label should return a 200
    * def result = call read('create-label.feature') { name: 'label2'}
    * def restPath = '/api/label/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'label2', id: '#(result.response.id)' }

  Scenario: retrieve root label should without READ permission should return a 403
    * def result = call read('create-label.feature') { name: 'label2'}
    * def restPath = '/api/label/'+result.response.id
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person',email: 'local.permissions@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path restPath
    When method GET
    Then status 403

  Scenario: retrieve a label without authentication should return a 401 error
    * def result = call read('create-label.feature') { name: 'label2'}
    * def restPath = '/api/label/'+result.response.id
    * configure headers = null
    Given path restPath
    When method GET
    Then status 401

  Scenario: update a root label should return a 200 and commit to audit log
    * def createResult = call read('create-label.feature') { name: 'label3'}
    * def labelId = createResult.response.id
    * def restPath = '/api/label/'+labelId
    Given path restPath
    And request { name: 'label4'}
    When method PUT
    Then status 200
    And match response == { name: 'label4', id: '#(labelId)' }
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'updateLabelById'
    And match stringResponse contains 'label'
    And match stringResponse contains 'labelId'


  Scenario: update a label without authorization should return a 401 error
    * def createResult = call read('create-label.feature') { name: 'label3'}
    * def labelId = createResult.response.id
    * def restPath = '/api/label/'+labelId
    * configure headers = null
    Given path restPath
    And request { name: 'label4'}
    When method PUT
    Then status 401

  Scenario: store a child label with valid name should return a 201
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * match childLabelResponse.response == { name: 'child', id: '#uuid', parentLabelId: '#(rootId)'}

  Scenario: delete a child label should return a 204
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    Given path restPath
    When method DELETE
    Then status 204

  Scenario: delete a root label should return a 204
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def rootRestPath = '/api/label/'+rootId
    Given path rootRestPath
    When method DELETE
    Then status 204
    * def childRestPath = '/api/label/'+childLabelResponse.response.id
    Given path childRestPath
    When method DELETE
    Then status 404
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'deleteLabelById'
    And match stringResponse contains 'label'

  Scenario: delete a child label without TREE_EDIT local permission should return a 403
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path restPath
    When method DELETE
    Then status 403

  Scenario: retrieve child label should return a 200
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'child', id: '#(childId)', parentLabelId: '#(rootId)' }

  Scenario: update a child label should return a 200
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    Given path restPath
    And request { name: 'label4', parentLabelId: '#(rootId)'}
    When method PUT
    Then status 200
    And match response == { name: 'label4', id: '#(childId)', parentLabelId: '#(rootId)' }

  Scenario: update a child label without any TREE_EDIT local permission should return a 403
    * def rootLabelResponse = call read('create-label.feature') { name: 'parent'}
    * def rootId = rootLabelResponse.response.id
    * def childLabelResponse = call read('create-label.feature') { name: 'child', parentLabelId: '#(rootId)'}
    * def childId = childLabelResponse.response.id
    * def restPath = '/api/label/'+childId
    * def userWithoutPermissions = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutPermissions.token)}
    Given path restPath
    And request { name: 'label4', parentLabelId: '#(rootId)'}
    When method PUT
    Then status 403


