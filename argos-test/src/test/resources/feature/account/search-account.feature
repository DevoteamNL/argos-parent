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

Feature: Search Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyId = defaultTestData.serviceAccount['default-sa2'].keyId
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}

  Scenario: search account by key id should return a 200
    Given path '/api/supplychain/'+supplyChain.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/account/search-account-keyinfo-response.json')
    And match response contains expectedResponse

  Scenario: search account by key id without READ should return a 403
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/supplychain/'+supplyChain.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 403
