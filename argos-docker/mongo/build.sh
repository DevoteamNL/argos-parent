#!/bin/sh
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

set -x
MONGO_VERSION="ff"
REGISTRY="argosnotary"
IMAGE_NAME="mongo"
build_image=${REGISTRY}/${IMAGE_NAME}:${MONGO_VERSION}


image() {
  echo "Build image ${build_image}"
  docker build \
    --tag ${build_image} \
  .
}

push() {
  image
  docker push ${build_image}
}

help() {
  echo "Usage: ./build.sh <function>"
  echo ""
  echo "Functions"
  printf "   \033[36m%-30s\033[0m %s\n" "image" "Build the Docker image."
  printf "   \033[36m%-30s\033[0m %s\n" "push" "Push the Docker image to the registry."
  echo ""
}

if [ -z "${1}" ]; then
  echo "ERROR: function required"
  help
  exit 1
fi
${1}