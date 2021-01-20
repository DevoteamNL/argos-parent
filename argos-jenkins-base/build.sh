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
name_space=$(grep "name_space=" build.config | cut -d '=' -f2)
docker_from_image=$(grep "docker_from_image=" build.config | cut -d '=' -f2)
latest_tag=$(grep "latest_tag" build.config | cut -d '=' -f2)

REGISTRY="${name_space}"
JENKINS_NAME=$(grep "jenkins_name=" build.config | cut -d '=' -f2)
IMAGE_LATEST="${JENKINS_NAME}:${latest_tag}"

image() {
  build_image=${REGISTRY}/${IMAGE_LATEST}
  echo "Build image ${build_image}"
  docker build \
    --tag ${build_image} \
  .
}

push() {
  image
  docker push ${REGISTRY}/${JENKINS_NAME}:${latest_tag}
}

help() {
  echo "Usage: ./build.sh <function>"
  echo ""
  echo "Functions"
  printf "   \033[36m%-30s\033[0m %s\n" "image" "Build the Docker image."
  printf "   \033[36m%-30s\033[0m %s\n" "push_final" "Push the Docker image to the internal registry."
  echo ""
  echo "Content of build.config"
  printf "   \033[36m%-30s\033[0m %s\n" "name=<name>" "Name of the Docker image, required."
  echo ""
}

if [ -z "${1}" ]; then
  echo "ERROR: function required"
  help
  exit 1
fi
${1}