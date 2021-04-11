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

Feature: Verification

  Background:
    * call read('classpath:feature/reset.feature')
    * def defaultVerificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def defaultSteps = [{link:'build-step-link.json', signingKey:2},{link:'test-step-link.json', signingKey:3}]
    * def defaultTestData = call read('classpath:default-test-data.js')
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}

  Scenario: happy flow all rules and commit to audit log
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'performVerification'
    And match stringResponse contains 'verifyCommand'

  Scenario: products to verify wrong hash
    * def verificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '0123456789012345678901234567890012345678901234567890123456789012'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: expected expected end products not matches
    * def verificationRequest = {expectedProducts: [{uri: 'argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: multi step happy flow all rules
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3}]
    * def verificationRequest = {expectedProducts: [{uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-happy-flow',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multi segment happy flow with three segment hop
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3},{link:'segment-3-build-step-link.json', signingKey:2},{link:'segment-3-test-step-link.json', signingKey:2}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-happy-flow-with-three-segment-hop',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multi segment with multiple verification context
    * def steps = [{link:'segment-1-build-step-link.json', signingKey:2},{link:'segment-1-test-step-link.json', signingKey:2},{link:'segment-2-build-step-link.json', signingKey:3},{link:'segment-2-build-step-link-invalid.json', signingKey:3},{link:'segment-2-test-step-link.json',signingKey:3},{link:'segment-2-test-step-link-invalid.json',signingKey:3},{link:'segment-3-build-step-link.json', signingKey:2},{link:'segment-3-test-step-link.json', signingKey:2}]
    * def verificationRequest = {expectedProducts: [ {uri: 'target/argos-test-0.0.1-SNAPSHOT.jar',hash: '49e73a11c5e689db448d866ce08848ac5886cac8aa31156ea4de37427aca6162'}] }
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(verificationRequest) ,testDir: 'multi-segment-with-multiple-verification-context',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest) ,testDir: 'match-rule-happy-flow',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-happy-flow-with-prefix
    * def resp = call read('classpath:feature/verification/verification-template.feature') {verificationRequest:#(defaultVerificationRequest) ,testDir: 'match-rule-happy-flow-with-prefix',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: happy flow match-rule-no-destination-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'match-rule-no-destination-artifact',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: happy flow match-rule-no-source-artifact
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'match-rule-no-source-artifact',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: build-steps-incomplete-run
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'build-steps-incomplete-run',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: delete-rule-no-deletion
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'delete-rule-no-deletion',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: create-rule-no-creation
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'create-rule-no-creation',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: modify-rule-not-modified
    * def resp = call read('classpath:feature/verification/verification-template.feature')  { verificationRequest:#(defaultVerificationRequest),testDir: 'modify-rule-not-modified',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: require-rule-no-required-product-material
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'require-rule-no-required-product-material',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: disallow-rule-non-empty
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'disallow-rule-non-empty',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: allow-rule-no-match
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'allow-rule-no-match',steps:#(defaultSteps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":false}

  Scenario: multiple-run-id-happy-flow
    * def steps = [{link:'runid1-build-step-link.json', signingKey:2},{link:'runid1-test-step-link.json', signingKey:3},{link:'runid2-build-step-link.json', signingKey:2},{link:'runid2-test-step-link.json',signingKey:3}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'multiple-run-id-happy-flow',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multiple-link-files-per-step-one-invalid
    * def steps = [{link:'build-step-link1.json', signingKey:2},{link:'build-step-link2.json', signingKey:2},{link:'test-step-link1.json', signingKey:2},{link:'test-step-link2.json',signingKey:2}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'multiple-link-files-per-step-one-invalid',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}

  Scenario: multiple-verification-contexts-happy-flow
    * def steps = [{link:'build-step-link-valid.json', signingKey:2},{link:'build-step-link-invalid.json', signingKey:3},{link:'test-step-link-invalid.json', signingKey:2},{link:'test-step-link-valid.json',signingKey:3}]
    * def resp = call read('classpath:feature/verification/verification-template.feature') { verificationRequest:#(defaultVerificationRequest),testDir: 'multiple-verification-contexts',steps:#(steps),layoutSigningKey:1}
    And match resp.response == {"runIsValid":true}
   

  Scenario: verification without authorization should return a 401 error
    * url karate.properties['server.baseurl']
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * configure headers = null
    Given path supplyChainPath + '/verification'
    And request defaultVerificationRequest
    When method POST
    Then status 401

  Scenario: verification without permission READ should return a 403 error
    * url karate.properties['server.baseurl']
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def supplyChainPath = '/api/supplychain/'+ supplyChain.response.id
    * def accountWithNoReadPermission = call read('classpath:feature/account/create-personal-account.feature') {name: 'Verify unauthorized person',email: 'local.noverify@extra.nogo'}
    * configure headers = call read('classpath:headers.js') { token: #(accountWithNoReadPermission.response.token)}
    Given path supplyChainPath + '/verification'
    And request defaultVerificationRequest
    When method POST
    Then status 403

  Scenario: SERVICE_ACCOUNT in other root label cannot verify
    * url karate.properties['server.baseurl']
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root1'}
    * def otherRootLabel = call read('classpath:feature/label/create-label.feature') { name: 'other-root-label'}
    * def personalAccount = defaultTestData.personalAccounts['default-pa1']
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount.accountId), labelId: #(rootLabel.response.id), permissions: [READ, TREE_EDIT]}
    * call read('classpath:feature/account/set-local-permissions.feature') {accountId: #(personalAccount.accountId), labelId: #(otherRootLabel.response.id), permissions: [READ, TREE_EDIT]}
    * configure headers = call read('classpath:headers.js') { token: #(personalAccount.token)}
    * call read('classpath:feature/account/create-service-account-with-key.feature') {accountName: 'sa6', parentLabelId: #(rootLabel.response.id), keyFile: 'sa-keypair1'}
    * def otherSupplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') {supplyChainName: other-supply-chain, parentLabelId: #(otherRootLabel.response.id)}
    * def keyPair = read('classpath:testmessages/key/sa-keypair1.json')
    * configure headers = call read('classpath:headers.js') { username: #(keyPair.keyId),password:#(keyPair.hashedKeyPassphrase)}
    Given path '/api/supplychain/'+ otherSupplyChain.response.id + '/verification'
    And request defaultVerificationRequest
    When method POST
    Then status 403
