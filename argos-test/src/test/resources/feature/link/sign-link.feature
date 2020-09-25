#
# Copyright (C) 2020 Argos Notary Coöperatie UA
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

@ignore
Feature: sign link

  Background:
    * url karate.properties['server.integration-test-service.baseurl']
    * def linkToBeSigned = __arg.json
    * def keyNumber = __arg.keyNumber
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyPair = defaultTestData.serviceAccount['default-sa'+keyNumber]

  Scenario: sign the layout should return 200
    Given path '/integration-test/signLinkMetaBlock'
    And request linkToBeSigned
    And param keyId = keyPair.keyId
    And param password = keyPair.passphrase
    When method POST
    Then status 200