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

Feature: Permissions

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')

  Scenario: get oauth providers return 200
    Given path '/api/oauthprovider'
    And method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/oauthprovider/oauth-response.json')
    And match response == expectedResponse


