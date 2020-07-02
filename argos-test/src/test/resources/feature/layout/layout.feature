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

Feature: Layout

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def accountWithNoReadPermissions = call read('classpath:feature/account/create-personal-account.feature') {name: 'account with no read permissions person',email: 'local.permissions@LAYOUT_ADD.go'}
    * call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(accountWithNoReadPermissions.response.id),labelId: #(supplyChain.response.parentLabelId), permissions: ["LAYOUT_ADD", "LINK_ADD"]}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * def validLayout = 'classpath:testmessages/layout/valid-layout.json'
    * def accountWithLayoutAddPermission = defaultTestData.personalAccounts['default-pa1']
    * configure headers = call read('classpath:headers.js') { token: #(accountWithLayoutAddPermission.token)}
    * def tokenWithoutLayoutAddPermissions = defaultTestData.adminToken

  Scenario: store layout with valid specifications should return a 200 and commit to auditlog
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createOrUpdateLayout'
    And match stringResponse contains 'supplyChainId'
    And match stringResponse contains 'layout'


  Scenario: store layout with invalid specifications should return a 400 error
    Given path layoutPath
    And request read('classpath:testmessages/layout/invalid-layout.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-response.json')

  Scenario: store layout without authorization should return a 401 error
    * configure headers = null
    Given path layoutPath
    And header Content-Type = 'application/json'
    And request read(validLayout)
    When method POST
    Then status 401

  Scenario: store layout without LAYOUT_ADD permission should return a 403 error
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutLayoutAddPermissions)}
    * def layout2BSigned = read(validLayout)
    * def signedLayout = call read('classpath:feature/layout/sign-layout.feature') {json:#(layout2BSigned),keyNumber:1}
    Given path layoutPath
    And request signedLayout.response
    When method POST
    Then status 403

  Scenario: find layout with valid supplychainid should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    Given path layoutPath
    When method GET
    Then status 200
    * def layoutId = layoutResponse.response.id
    * def expected = read('classpath:testmessages/layout/valid-layout-response.json')
    And match response contains expected

  Scenario: find layout without authorization should return a 401 error
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = null
    Given path layoutPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: find layout without READ permission should return a 403
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermissions.response.token)}
    Given path layoutPath
    When method GET
    Then status 403

  Scenario: update a layout should return a 200
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:1}
    Given path layoutPath
    And request requestBody.response
    When method POST
    Then status 201
    Given path layoutPath
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/layout/valid-update-layout-response.json')
    And match response contains expectedResponse

  Scenario: update a layout without LAYOUT_ADD permission should return a 403
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutLayoutAddPermissions)}
    Given path layoutPath
    And request requestBody.response
    When method POST
    Then status 403

  Scenario: update a layout without authorization should return a 401 error
    * def layoutResponse = call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * def layoutToBeSigned = read('classpath:testmessages/layout/valid-update-layout.json')
    * def requestBody = call read('sign-layout.feature') {json:#(layoutToBeSigned),keyNumber:1}
    * configure headers = null
    Given path layoutPath
    And request requestBody.response
    And header Content-Type = 'application/json'
    When method POST
    Then status 401

  Scenario: validate layout with invalid model specifications should return a 400 error
    Given path layoutPath+'/validate'
    And request read('classpath:testmessages/layout/invalid-layout-validation.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-validation-response.json')

  Scenario: validate layout with data input  should return a 400 error
    Given path layoutPath+'/validate'
    And request read('classpath:testmessages/layout/invalid-layout-data-validation.json')
    When method POST
    Then status 400
    And match response contains read('classpath:testmessages/layout/invalid-layout-validation-data-response.json')

  Scenario: validate layout with valid specifications should return a 204
    Given path layoutPath+'/validate'
    And request read('classpath:testmessages/layout/valid-layout-validation.json')
    When method POST
    Then status 204

  Scenario: create ApprovalConfiguration should return a 201
    * def response = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}

  Scenario: create ApprovalConfiguration with repeated posts should return a 200
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-multiple.json')
    When method POST
    Then status 200
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-multiple.json')
    When method POST
    Then status 200
    And match response == read('classpath:testmessages/layout/approval-config-create-multiple-response.json')

  Scenario: create ApprovalConfiguration without LAYOUT_ADD permission should return a 403
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutLayoutAddPermissions)}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-request.json')
    When method POST
    Then status 403

  Scenario: get approvalConfigurations should return a 200
    * def createcall = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}
    Given path layoutPath+'/approvalconfig/'
    When method GET
    Then status 200
    And match response == read('classpath:testmessages/layout/approval-configs-response.json')

  Scenario: get approvalConfigurations with no READ permission should return a 403
    * def createcall = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermissions.response.token)}
    Given path layoutPath+'/approvalconfig/'
    When method GET
    Then status 403


  Scenario: get personal approvalConfigurations should return a 200
    * def createcall = call read('create-approval-config.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1,layoutPath:#(layoutPath)}
    Given path layoutPath+'/approvalconfig'
    And request read('classpath:testmessages/layout/approval-config-create-multiple.json')
    When method POST
    Then status 200
    * configure headers = call read('classpath:headers.js') { token: #(accountWithLayoutAddPermission.token)}
    Given path layoutPath+'/approvalconfig/me'
    When method GET
    Then status 200
    And match response == read('classpath:testmessages/layout/approval-configs-response-me.json')

  Scenario: get personal approvalConfigurations without LINK_ADD permission should return a 403
    * call read('create-layout.feature') {supplyChainId:#(supplyChain.response.id), json:#(validLayout), keyNumber:1}
    * configure headers = call read('classpath:headers.js') { token: #(tokenWithoutLayoutAddPermissions)}
    Given path layoutPath+'/approvalconfig/me'
    When method GET
    Then status 403