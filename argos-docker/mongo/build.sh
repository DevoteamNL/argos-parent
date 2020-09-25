#!/bin/sh
#
# Copyright (C) 2020 Argos Notary Coöperatie UA
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