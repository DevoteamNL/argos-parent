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

Feature: Search Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyId = defaultTestData.serviceAccount['default-sa2'].keyId
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}

  Scenario: search account by key id should return a 200
    Given path '/api/supplychain/'+supplyChain.response.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/account/search-account-keyinfo-response.json')
    And match response contains expectedResponse

  Scenario: search account by key id without READ should return a 403
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/supplychain/'+supplyChain.response.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 403

  Scenario: search account by name should return a 200
    Given path '/api/supplychain/'+supplyChain.response.id+'/account'
    And param name = "default-sa1"
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/account/search-account-info-response.json')
    And match response contains expectedResponse

  Scenario: search account without READ should return a 403
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/supplychain/'+supplyChain.response.id+'/account'
    And param name = "default-sa1"
    When method GET
    Then status 403

  Scenario: search account by name not in path should return a 200 with empty array
    * def root1 = call read('classpath:feature/label/create-label.feature') { name: 'root1'}
    * def personalAccount = defaultTestData.personalAccounts['default-pa1']
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount.accountId), labelId: #(root1.response.id), permissions: [READ, TREE_EDIT]}
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount.token)}
    * call read('create-service-account.feature') { name: 'not-in-path', parentLabelId: #(root1.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    Given path '/api/supplychain/'+supplyChain.response.id+'/account'
    And param name = 'not-in-path'
    When method GET
    Then status 200
    And match response == []
