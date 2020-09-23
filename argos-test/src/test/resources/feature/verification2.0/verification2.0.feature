#
# Copyright (C) 2020 Argos Notary Cooperative
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

Feature: Verification2.0

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultReleaseRequest = {releaseArtifacts: [[{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}]] }
    * def defaultSteps = [{link:'build-step-link.json', signingKey:2},{link:'test-step-link.json', signingKey:3}]
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}

  Scenario: successfull release should return successfull verify
    * def resp = call read('classpath:feature/release/release-template.feature') { releaseRequest:#(defaultReleaseRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    * configure headers = {'Content-Type': 'application/json'}
    Given path '/api/supplychain/verification'
    And param artifactHashes = '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'
    And param path = 'default-root-label,other-root-label'
    When method GET
    Then status 200
    And match response == {"runIsValid":true}

  Scenario: successfull release with incorrect hash should return unsuccessfull verify
    * def resp = call read('classpath:feature/release/release-template.feature') { releaseRequest:#(defaultReleaseRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    * configure headers = {'Content-Type': 'application/json'}
    Given path '/api/supplychain/verification'
    And param artifactHashes = '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6163'
    And param path = 'default-root-label'
    When method GET
    Then status 200
    And match response == {"runIsValid":false}


