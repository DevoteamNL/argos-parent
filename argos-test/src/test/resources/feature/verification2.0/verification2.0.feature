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


