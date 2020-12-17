#
# Copyright (C) 2020 Argos Notary
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

Feature: Hierarchy

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def root1 = call read('classpath:feature/label/create-label.feature') { name: 'root1'}
    * def root2 = call read('classpath:feature/label/create-label.feature') { name: 'root2'}
    * def root3 = call read('classpath:feature/label/create-label.feature') { name: 'root3'}
    * def personalAccount = defaultTestData.personalAccounts['default-pa1']
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount.accountId), labelId: #(root1.response.id), permissions: [READ, SERVICE_ACCOUNT_EDIT,TREE_EDIT]}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount.accountId), labelId: #(root2.response.id), permissions: [READ, SERVICE_ACCOUNT_EDIT,TREE_EDIT]}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount.accountId), labelId: #(root3.response.id), permissions: [READ, SERVICE_ACCOUNT_EDIT,TREE_EDIT]}
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount.token)}
    * def root1ChildResponse = call read('classpath:feature/label/create-label.feature') { name: 'childaroot1',parentLabelId:#(root1.response.id)}
    * def root2ChildResponse = call read('classpath:feature/label/create-label.feature') { name: 'childaroot2',parentLabelId:#(root2.response.id)}
    * def root3ChildResponse = call read('classpath:feature/label/create-label.feature') { name: 'childaroot3',parentLabelId:#(root3.response.id)}
    * def supplyChain1Response = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-1, parentLabelId: #(root1ChildResponse.response.id)}
    * def supplyChain2Response = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-2, parentLabelId: #(root2ChildResponse.response.id)}
    * def supplyChain3Response = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-3, parentLabelId: #(root3ChildResponse.response.id)}
    * def root1Child2Response = call read('classpath:feature/label/create-label.feature') { name: 'childbroot1',parentLabelId:#(root1.response.id)}
    * def root2Child2Response = call read('classpath:feature/label/create-label.feature') { name: 'childbroot2',parentLabelId:#(root2.response.id)}
    * def root3Child2Response = call read('classpath:feature/label/create-label.feature') { name: 'childbroot3',parentLabelId:#(root3.response.id)}
    * def serviceAccount1Response = call read('classpath:feature/account/create-service-account.feature') {name: sa-1, parentLabelId: #(root1ChildResponse.response.id)}
    * def serviceAccount2Response = call read('classpath:feature/account/create-service-account.feature') {name: sa-2, parentLabelId: #(root2ChildResponse.response.id)}
    * def serviceAccount3Response = call read('classpath:feature/account/create-service-account.feature') {name: sa-3, parentLabelId: #(root3ChildResponse.response.id)}


  Scenario: get root nodes with HierarchyMode all should return full trees
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-all.json')
    And match response == expectedResponse

  Scenario: get root nodes with default user and no hierarchy permissions should return empty array
    * def userWithoutHierarchyPermissions = defaultTestData.personalAccounts['default-pa2']
    * configure headers = call read('classpath:headers.js') { token: #(userWithoutHierarchyPermissions.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    And match response == []

  Scenario: get root nodes without authorization should return a 401 error
    * configure headers = null
    Given path '/api/hierarchy'
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 401

  Scenario: get root nodes with HierarchyMode none should return root entries only
    Given path '/api/hierarchy'
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-none.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode maxdepth should return maxdepth descendant entries only
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1root1',parentLabelId:#(root1ChildResponse.response.id)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = 1
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-maxdepth.json')
    And match response == expectedResponse

  Scenario: get root nodes with HierarchyMode maxdepth and non positive maxdepth should return validation error
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = -1
    When method GET
    Then status 400
    And match response == {"messages": [{"field": "getRootNodes.maxDepth","type": "DATA_INPUT","message": "must be greater than or equal to 1"}]}

  Scenario: get root nodes with HierarchyMode maxdepth and no maxdepth should return maxdepth 1 descendant entries only
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildResponse.response.id)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'MAX_DEPTH'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-maxdepth.json')
    And match response == expectedResponse

  Scenario: get root nodes with no permissions should return only root nodes with permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person',email: 'local.permissions@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def localPermissionsForRoot = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(extraAccount.response.id),labelId: #(root1.response.id), permissions: ["READ"]}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/hierarchy'
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-rootnodes-partial-permissions.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode all should return full tree
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-all.json')
    And match response == expectedResponse

  Scenario: get subtree with added permissions downtree should return correct permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person',email: 'local.permissions@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def localPermissionsForRoot = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(extraAccount.response.id),labelId: #(root1.response.id), permissions: ["READ"]}
    * def root1ChildPermissions = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(extraAccount.response.id),labelId: #(root1ChildResponse.response.id), permissions: ["LOCAL_PERMISSION_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-added-local-permissions.json')
    And match response == expectedResponse

  Scenario: get subtree with permissions uptree should return correct partial hierarchy
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person',email: 'local.permissions@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def root1ChildPermissions = call read('classpath:feature/account/set-local-permissions.feature') { accountId: #(extraAccount.response.id),labelId: #(root1ChildResponse.response.id), permissions: ["LOCAL_PERMISSION_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'ALL'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-partial-hierarchy.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode none should return only root
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'NONE'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-none.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode max depth 1 should return only direct descendants
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildResponse.response.id)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = 1
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-maxdepth.json')
    And match response == expectedResponse

  Scenario: get subtree with HierarchyMode max depth -1 should return a validation error
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    And param maxDepth = -1
    When method GET
    Then status 400
    And match response ==  {"messages": [{"field": "getSubTree.maxDepth","type": "DATA_INPUT","message": "must be greater than or equal to 1"}]}

  Scenario: get subtree with HierarchyMode maxdepth and no maxdepth should return maxdepth 1 descendant entries only
    * call read('classpath:feature/label/create-label.feature') { name: 'subchild1child3root1',parentLabelId:#(root1ChildResponse.response.id)}
    Given path '/api/hierarchy/' + root1.response.id
    And param HierarchyMode = 'MAX_DEPTH'
    When method GET
    Then status 200
    * def expectedResponse =  read('classpath:testmessages/hierarchy/expected-hierarchy-subtree-maxdepth.json')
    And match response == expectedResponse