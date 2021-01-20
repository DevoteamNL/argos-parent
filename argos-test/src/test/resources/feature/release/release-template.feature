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

@Ignore
Feature: Verification template

  Background:
    * url karate.properties['server.baseurl']
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def releaseRequest = __arg.releaseRequest
    * def testFilesDir = __arg.testDir
    * def steps = __arg.steps
    * def layoutSigningKey = __arg.layoutSigningKey
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}
    * def layoutPath = '/api/supplychain/'+ supplyChain.response.id + '/layout'
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * def supplyChainId = supplyChain.response.id

  Scenario: run template
    Given print 'testFilesDir : ', testFilesDir
    * def layout = 'classpath:testmessages/verification/'+testFilesDir+'/layout.json'
    * def layoutCreated = call read('classpath:feature/layout/create-layout.feature') {supplyChainId:#(supplyChainId), json:#(layout), keyNumber:#(layoutSigningKey)}
    # this creates an array of stepLinksJson messages
    * def stepLinksJsonMapper = function(jsonlink, i){ return  {supplyChainId:supplyChainId, json:'classpath:testmessages/verification/'+testFilesDir+'/'+jsonlink.link, keyNumber:jsonlink.signingKey}}
    * def stepLinksJson = karate.map(steps, stepLinksJsonMapper)
    # when a call to a feature presented with an array of messages it will cal the feature template iteratively
    * call read('classpath:feature/link/create-link.feature') stepLinksJson
    * def keyPair = defaultTestData.serviceAccount['default-sa1']
    * configure headers = call read('classpath:headers.js') { username: #(keyPair.keyId), password: #(keyPair.hashedKeyPassphrase)}
    Given path supplyChainPath + '/release'
    And request  releaseRequest
    When method POST
    Then status 200