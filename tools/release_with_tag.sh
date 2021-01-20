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
    echo "Usage: ./release_with_tag.sh  <release version>"
    echo
    echo "This will force a release of the project with the <release version>"
}

push() {
    git tag -f ${VERSION}
    git push -f origin refs/tags/${VERSION}
}

if [ -z "$1" ]; then
    printf "\033[31m%-5s\033[0m %s\n" "ERROR" "No version parameter."
    usage
    exit 8
fi

VERSION=$1

printf "\033[33m%-5s\033[0m %s\n" "WARN" "This will force a release of the project with the version ${VERSION}"
printf "\033[33m%-5s\033[0m %s\n" "WARN" "with a force push of the tag ${VERSION}"
while true; do
    read -p "Are you sure you wish to do this [y/n]: " yn
    case $yn in
        [Yy]* ) push; break;;
        [Nn]* ) echo "Abort..."; exit;;
        * ) echo "Please answer yes(y) or no(n).";;
    esac
done
