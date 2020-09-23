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

@ignore
Feature: using __arg

  Background:
    * url karate.properties['server.baseurl']
    * def accountName = __arg.accountName
    * def parentLabelId = __arg.parentLabelId;
    * def keyFile = __arg.keyFile;

  Scenario: create an sa account with active key
    * def accountResponse = call read('classpath:feature/account/create-service-account.feature') {name: #(accountName), parentLabelId: #(parentLabelId)}
    * def key = read('classpath:testmessages/key/'+keyFile+'.json')
    * call read('classpath:feature/account/create-service-account-key.feature') {accountId: #(accountResponse.response.id),key: #(key)}