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