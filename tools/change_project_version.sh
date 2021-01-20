#!/bin/bash
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


set -e

usage() {
    echo "Usage: ./change_project_version.sh  <release version>"
}

if [ -z "$1" ]; then
    printf "\033[31m%-5s\033[0m %s\n" "ERROR" "No version parameter."
    usage
    exit 8
fi

VERSION=$1

SCRIPT_DIRECTORY=$(cd `dirname $0` && pwd)

PARENT_DIR="${SCRIPT_DIRECTORY}/.."

printf "\033[33m%-5s\033[0m %s\n" "INFO" "Update version in all modules to ${VERSION}"
mvn -q -f ${PARENT_DIR}/pom.xml versions:set -DnewVersion=${VERSION} -DprocessAllModules=true -DgenerateBackupPoms=false
