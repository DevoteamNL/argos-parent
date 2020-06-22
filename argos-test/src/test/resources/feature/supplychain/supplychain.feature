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

Feature: SupplyChain

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}

  Scenario: store supplychain with valid name should return a 201 and commit to audit log
    Given path '/api/supplychain'
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def locationHeader = supplyChain.responseHeaders['Location'][0]
    * match supplyChain.response == { name: 'name', id: '#uuid', parentLabelId: '#(defaultTestData.defaultRootLabel.id)' }
    * match locationHeader contains 'api/supplychain/'
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createSupplyChain'
    And match stringResponse contains 'supplyChain'

  Scenario: store supplychain with non unique name should return a 400
    * def supplyChainResponse = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    Given path '/api/supplychain'
    And request  {"name":"name", parentLabelId: "#(supplyChainResponse.response.parentLabelId)"}
    When method POST
    Then status 400
    And match response.messages[0].message contains 'supply chain with name: name and parentLabelId'

  Scenario: store supplychain without authorization should return a 401 error
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: otherlabel}
    * configure headers = null
    Given path '/api/supplychain'
    And request  {"name":"name", parentLabelId: "#(labelResult.response.id)"}
    And header Content-Type = 'application/json'
    When method POST
    Then status 401

  Scenario: store supplychain with local permission TREE_EDIT should return a 201
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["TREE_EDIT"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain'
    And request  {"name":"name", parentLabelId: "#(info.labelId)"}
    When method POST
    Then status 201

  Scenario: store supplychain with local permission READ should return a 403
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}

    Given path '/api/supplychain'
    And request  {"name":"name", parentLabelId: "#(info.labelId)"}
    When method POST
    Then status 403

  Scenario: update supplychain should return a 200 and commit to audit log
    * def supplyChainResponse = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: otherlabel}
    Given path '/api/supplychain/'+supplyChainResponse.response.id
    And request  {"name":"supply-chain-name", parentLabelId: "#(labelResult.response.id)"}
    When method PUT
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#(supplyChainResponse.response.id)', parentLabelId: '#(labelResult.response.id)' }
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'updateSupplyChain'
    And match stringResponse contains 'supplyChainId'
    And match stringResponse contains 'supplyChain'

  Scenario: update supplychain with local permission TREE_EDIT should return a 200
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["TREE_EDIT"]}
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: sublabel, parentLabelId: #(info.labelId)}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: name, parentLabelId: #(labelResult.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain/'+supplyChain.response.id
    And request  {"name":"supply-chain-name", parentLabelId: "#(info.labelId)"}
    When method PUT
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#(supplyChain.response.id)', parentLabelId: '#(info.labelId)' }

  Scenario: update supplychain without local permission TREE_EDIT should return a 403
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: sublabel, parentLabelId: #(info.labelId)}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: name, parentLabelId: #(labelResult.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain/'+supplyChain.response.id
    And request  {"name":"supply-chain-name", parentLabelId: "#(info.labelId)"}
    When method PUT
    Then status 403

  Scenario: update supplychain without authorization should return a 401 error
    * def supplyChainResponse = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: otherlabel}
    * configure headers = null
    Given path '/api/supplychain/'+supplyChainResponse.response.id
    And request  {"name":"supply-chain-name", parentLabelId: "#(labelResult.response.id)"}
    And header Content-Type = 'application/json'
    When method PUT
    Then status 401

  Scenario: get supplychain with valid id should return a 200
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def restPath = '/api/supplychain/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'name', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: get supplychain with local permission READ should return a 200
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: name, parentLabelId: #(info.labelId)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def restPath = '/api/supplychain/'+supplyChain.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'name', id: '#(supplyChain.response.id)', parentLabelId: '#(info.labelId)' }

  Scenario: get supplychain without local permission READ should return a 403
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["LAYOUT_ADD"]}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: name, parentLabelId: #(info.labelId)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def restPath = '/api/supplychain/'+supplyChain.response.id
    Given path restPath
    When method GET
    Then status 403

  Scenario: get supplychain with implicit local permission READ should return a 200
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["TREE_EDIT"]}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: name, parentLabelId: #(info.labelId)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    * def restPath = '/api/supplychain/'+supplyChain.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'name', id: '#(supplyChain.response.id)', parentLabelId: '#(info.labelId)' }

  Scenario: get supplychain without authorization should return a 401 error
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def restPath = '/api/supplychain/'+result.response.id
    * configure headers = null
    Given path restPath
    And header Content-Type = 'application/json'
    When method GET
    Then status 401

  Scenario: get supplychain with invalid id should return a 404
    Given path '/api/supplychain/invalidid'
    When method GET
    Then status 404
    And match response == {"message":"supply chain not found : invalidid"}

  Scenario: query supplychain with name should return a 200
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'supply-chain-name'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'label'
    When method GET
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#uuid', parentLabelId: '#uuid' }
    
  Scenario: query supplychain with name and several labels should return a 200
    * def root = call read('classpath:feature/label/create-label.feature') { name: 'root'}
    * def rootChildResponse = call read('classpath:feature/label/create-label.feature') { name: 'childaroot',parentLabelId:#(root.response.id)}
    * def supplyChainResponse = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: supply-chain-1, parentLabelId: #(rootChildResponse.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-1'
    And param path = 'root,childaroot'
    When method GET
    Then status 200
    And match response == { name: 'supply-chain-1', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: query supplychain with local permission READ should return a 200
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: supply-chain-name, parentLabelId: #(info.labelId)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'otherlabel'
    When method GET
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#(supplyChain.response.id)', parentLabelId: '#(info.labelId)' }

  Scenario: query supplychain with sa account should return a 200
    * def keyPair = defaultTestData.serviceAccount['default-sa1']
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: supply-chain-name, parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * configure headers = call read('classpath:headers.js') { username: #(keyPair.keyId), password: #(keyPair.hashedKeyPassphrase)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'default_root_label'
    When method GET
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#(supplyChain.response.id)', parentLabelId: '#(defaultTestData.defaultRootLabel.id)' }

  Scenario: query supplychain with sa and incorrect search term should return a 403
    * def keyPair = defaultTestData.serviceAccount['default-sa1']
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: supply-chain-name, parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * configure headers = call read('classpath:headers.js') { username: #(keyPair.keyId), password: #(keyPair.hashedKeyPassphrase)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'incorrect search term'
    When method GET
    Then status 403

  Scenario: query supplychain without local permission READ should return a 403
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["LAYOUT_ADD"]}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: supply-chain-name, parentLabelId: #(info.labelId)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'otherlabel'
    When method GET
    Then status 403

  Scenario: query supplychain with implicit local permission READ should return a 200
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["TREE_EDIT"]}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: supply-chain-name, parentLabelId: #(info.labelId)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'otherlabel'
    When method GET
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#(supplyChain.response.id)', parentLabelId: '#(info.labelId)' }

  Scenario: query supplychain with name and non existing label should return a 404
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'supply-chain-name'}
    Given path '/api/supplychain'
    And param name = 'supply-chain-name'
    And param path = 'otherlabel'
    When method GET
    Then status 404

  Scenario: delete supplychain with valid id should return a 200
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def restPath = '/api/supplychain/'+result.response.id
    Given path restPath
    When method DELETE
    Then status 204
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'deleteSupplyChain'
    And match stringResponse contains 'supplyChainId'

  Scenario: delete supplychain without local permission TREE_EDIT should return a 403
    * def info = call read('classpath:create-local-authorized-account.js') {permissions: ["READ"]}
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: sublabel, parentLabelId: #(info.labelId)}
    * def supplyChain = call read('create-supplychain.feature') {supplyChainName: name, parentLabelId: #(labelResult.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(info.token)}
    Given path '/api/supplychain/'+supplyChain.response.id
    When method DELETE
    Then status 403
