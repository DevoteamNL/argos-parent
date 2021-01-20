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

Feature: Personal Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def token = karate.properties['bearer.token']
    * def defaultUsertoken = defaultTestData.personalAccounts['default-pa1'].token
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}

  Scenario: get Personal Account profile should return 200
    * def expectedResponse = read('classpath:testmessages/personal-account/admin-account-response.json')
    Given path '/api/personalaccount/me'
    When method GET
    Then status 200
    Then match response == expectedResponse

  Scenario: get logout should return 204
    Given path 'api/personalaccount/me/logout'
    And request ''
    When method PUT
    Then status 204
    * def expectedResponse = read('classpath:testmessages/personal-account/admin-account-response.json')
    Given path '/api/personalaccount/me'
    When method GET
    Then status 401

  Scenario: createKey should return 204 and commit to audit log
    Given path '/api/personalaccount/me/key'
    And request read('classpath:testmessages/key/personal-keypair.json')
    When method POST
    Then status 204
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'createKey'
    And match stringResponse contains 'keyPair'

  Scenario: createKey with invalid key should return 400
    Given path '/api/personalaccount/me/key'
    And request {"keyId": "invalidkeyid","publicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/Ldm84IhBvssdweZOZSPcx87J0Xy63g0JhlOYlr66aKmbXz5YD+J+b4NlIIbvaa5sEg4FS0+gkOPgexqCzgRUqHK5coLchpuLFggmDiL4ShqGIvqb/HPq7Aauk8Ss+0TaHfkJjd2kEBPRgWLII1gytjKkqlRGD/LxRtsppnleQwIDAQAB","encryptedPrivateKey": null}
    When method POST
    Then status 400

  Scenario: getKey should return 200
    * def keyPair = read('classpath:testmessages/key/personal-keypair.json')
    Given path '/api/personalaccount/me/key'
    And request keyPair
    When method POST
    Then status 204
    Given path '/api/personalaccount/me/key'
    When method GET
    Then status 200
    Then match response == keyPair


  Scenario: get account by id should return a 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * def expectedResponse = read('classpath:testmessages/personal-account/get-account-by-id-response.json')
    Given path '/api/personalaccount/'+extraAccount.response.id
    When method GET
    Then status 200
    Then match response == expectedResponse

  Scenario: get account by id without ASSIGN_ROLE should return a 403
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * def expectedResponse = read('classpath:testmessages/personal-account/get-account-by-id-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount/'+extraAccount.response.id
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: update roles should return 200 and commit to auditlog
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/role'
    And request ["ADMINISTRATOR"]
    When method PUT
    Then status 200
    Then match response == {"id":"#(extraAccount.response.id)","name":"Extra Person", "roles": ["ADMINISTRATOR"]}
    Given path '/api/personalaccount/'+extraAccount.response.id
    When method GET
    Then status 200
    Then match response == {"id":"#(extraAccount.response.id)","name":"Extra Person", "roles": ["ADMINISTRATOR"]}
    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'updatePersonalAccountRolesById'
    And match stringResponse contains 'accountId'
    And match stringResponse contains 'roleNames'


  #updatePersonalAccountRolesById

  Scenario: remove administrator role of administrator should return 400
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/role'
    And request ["ADMINISTRATOR"]
    When method PUT
    Then status 200
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/role'
    And request []
    When method PUT
    Then status 400
    And match response == {"messages": [{"type": "DATA_INPUT","message": "administrators can not unassign there own administrator role"}]}

  Scenario: user without ASSIGN_ROLE permission can not assign a role to a user
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/role'
    And request ["ADMINISTRATOR"]
    When method PUT
    Then status 403
    And match response == {"message":"Access denied"}
    
  Scenario: search personal account by role name should return 200 as admin
    Given path '/api/personalaccountwithroles'
    And param role = "ADMINISTRATOR"
    When method GET
    Then status 200
    And match response == [{"roles":["ADMINISTRATOR"],"id":"#uuid","name":"Luke Skywalker"}]

  Scenario: search personal account by active key id should return 200
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    And param activeKeyIds = 'f808d5d02e2738467bc818d6c54ee68bcf8d13e78c3b1d4d50d73cbfc87fd447'
    When method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"Default User"}]

  Scenario: search personal account by inactive key id should return 200
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    And param inactiveKeyIds = 'f808d5d02e2738467bc818d6c54ee68bcf8d13e78c3b1d4d50d73cbfc87fd447'
    When method GET
    Then status 200
    And match response == []

  Scenario: search all personal account 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * def expectedResponse = read('classpath:testmessages/personal-account/account-search-all-response.json')
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    When method GET
    Then status 200
    And match response == expectedResponse

  Scenario: search personal account without authentication should return a 401
    * configure headers = call read('classpath:headers.js') { token: ""}
    Given path '/api/personalaccount'
    When method GET
    Then status 401

  Scenario: search personal account by name should return 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'extra@extra.go'}
    * configure headers = call read('classpath:headers.js') { token: #(defaultUsertoken)}
    Given path '/api/personalaccount'
    And param name = 'per'
    When method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"Extra Person"}]

  Scenario: search by local permission label id personal account 200
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'search@extra.go'}
    * def label = call read('classpath:feature/label/create-label.feature') { name: 'label1'}
    Given path '/api/personalaccount'
    And param localPermissionsLabelId = label.response.id
    When method GET
    Then status 200
    And match response == []

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request ["READ"]
    When method PUT
    Then status 200

    Given path '/api/personalaccount'
    And param localPermissionsLabelId = label.response.id
    When method GET
    Then status 200
    And match response == [{"id":"#uuid","name":"Extra Person"}]

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request []
    When method PUT
    Then status 204

    Given path '/api/personalaccount'
    And param localPermissionsLabelId = label.response.id
    When method GET
    Then status 200
    And match response == []

  Scenario: manage local permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'local.permissions@extra.go'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission'
    When method GET
    Then status 200
    And match response == []
    * def label = call read('classpath:feature/label/create-label.feature') { name: 'label1'}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    When method GET
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": []}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request ["READ"]
    When method PUT
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": ["READ"]}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    And request ["READ"]
    When method PUT
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": ["READ"]}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+label.response.id
    When method GET
    Then status 200
    And match response == {"labelId": "#(label.response.id)", "permissions": ["READ"]}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission'
    When method GET
    Then status 200
    And match response == [{"labelId": "#(label.response.id)", "permissions": ["READ"]}]

    * def auditlog = call read('classpath:feature/auditlog.feature')
    * string stringResponse = auditlog.response
    And match stringResponse contains 'updateLocalPermissionsForLabel'
    And match stringResponse contains 'accountId'
    And match stringResponse contains 'labelId'
    And match stringResponse contains 'localPermissions'


  Scenario: a user with local permission LOCAL_PERMISSION_EDIT can manage local permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email:'local.permissions@extra.go'}
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root-label'}
    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+rootLabel.response.id
    And request ["LOCAL_PERMISSION_EDIT"]
    When method PUT
    Then status 200

    * def childLabel = call read('classpath:feature/label/create-label.feature') { name: 'child-label', parentLabelId: #(rootLabel.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    * def anotherAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Another Person', email: 'another@extra.go'}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    And request ["READ"]
    When method PUT
    Then status 200

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    When method GET
    Then status 200
    And match response == {"labelId": "#(childLabel.response.id)", "permissions": ["READ"]}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission'
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: a user without local permission LOCAL_PERMISSION_EDIT can not manage local permissions
    * def extraAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Extra Person', email: 'local.permissions@extra.go'}
    * def rootLabel = call read('classpath:feature/label/create-label.feature') { name: 'root-label'}

    Given path '/api/personalaccount/'+extraAccount.response.id+'/localpermission/'+rootLabel.response.id
    And request ["READ"]
    When method PUT
    Then status 200

    * def childLabel = call read('classpath:feature/label/create-label.feature') { name: 'child-label', parentLabelId: #(rootLabel.response.id)}
    * configure headers = call read('classpath:headers.js') { token: #(extraAccount.response.token)}
    * def anotherAccount = call read('classpath:feature/account/create-personal-account.feature') {name: 'Another Person', email: 'another@extra.go'}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    And request ["READ"]
    When method PUT
    Then status 403
    And match response == {"message":"Access denied"}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission/'+childLabel.response.id
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

    Given path '/api/personalaccount/'+anotherAccount.response.id+'/localpermission'
    When method GET
    Then status 403
    And match response == {"message":"Access denied"}

  Scenario: get account by id should return a 200
    Given path '/api/personalaccount/'+defaultTestData.personalAccounts['default-pa1'].accountId+'/key'
    When method GET
    Then status 200
    Then match response == {keyId: #(defaultTestData.personalAccounts['default-pa1'].keyId), publicKey: #(defaultTestData.personalAccounts['default-pa1'].publicKey)}

  Scenario: search personal account not authenticated should return a 401
    * configure headers = call read('classpath:headers.js') { token: ""}
    Given path '/api/personalaccount/'+defaultTestData.personalAccounts['default-pa1'].accountId+'/key'
    When method GET
    Then status 401

  Scenario: should refresh token
    * def extraToken = call read('classpath:feature/account/create-token.feature') {accountId: #(defaultTestData.personalAccounts['default-pa1'].accountId), minutesEarlier: 18}
    * configure headers = call read('classpath:headers.js') { token: #(extraToken.response.token)}
    Given path '/api/personalaccount/me'
    When method GET
    Then status 401
    Then match response.message == 'refresh token'

  Scenario: refresh token
    * def extraToken = call read('classpath:feature/account/create-token.feature') {accountId: #(defaultTestData.personalAccounts['default-pa1'].accountId), minutesEarlier: 32}
    * configure headers = call read('classpath:headers.js') { token: #(extraToken.response.token)}
    Given path '/api/personalaccount/me'
    When method GET
    Then status 401
    Then match response.message == 'refresh token'
    Given path '/api/personalaccount/me/refresh'
    When method GET
    Then status 200
    * configure headers = call read('classpath:headers.js') { token: #(response.token)}
    Given path '/api/personalaccount/me'
    When method GET
    Then status 200
    Then match response.name == 'Default User'

  Scenario: token session expired
    * def extraToken = call read('classpath:feature/account/create-token.feature') {accountId: #(defaultTestData.personalAccounts['default-pa1'].accountId), minutesEarlier: 62}
    * configure headers = call read('classpath:headers.js') { token: #(extraToken.response.token)}
    Given path '/api/personalaccount/me'
    When method GET
    Then status 401
    Then match response == ''

  Scenario: token session expired can not refresh token
    * def extraToken = call read('classpath:feature/account/create-token.feature') {accountId: #(defaultTestData.personalAccounts['default-pa1'].accountId), minutesEarlier: 720}
    * configure headers = call read('classpath:headers.js') { token: #(extraToken.response.token)}
    Given path '/api/personalaccount/me/refresh'
    When method GET
    Then status 401
    Then match response == ''
