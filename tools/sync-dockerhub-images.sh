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

if [ -z "$@" ]
then
  echo "Usage: $0 <destination registry> [namespace/]<image>[:version]"
  exit 1
fi

image=$2
shift

registry_src="hub.docker.com"
registry_dst=$1

echo "Processing ${image}"

printf "Determine source namespace: "
if [[ ${image} == *"/"* ]]; then
  namespace="$(echo ${image} | cut -d '/' -f 1)"
else
  namespace="library"
fi
echo $namespace

if [[ ${image} == *":"* ]]; then
  image_version="$(echo $image | cut -d ':' -f 2)"
else
  image_version=""
fi

printf "Determine image name: "
image_name="$(echo ${image} | cut -d: -f1 | awk -F/ '{print $NF}')"
echo $image_name

if [ "${image_version}" == "" ]; then
  echo "Getting all tags on '${registry_src}'"
  image_versions=$(curl -s https://${registry_src}/v2/repositories/${namespace}/${image_name}/tags/?page_size=500 | jq -r '.results|.[]|.name')
else
  image_versions=${image_version}
fi
printf "Image version(s): "
echo $image_versions

for version in ${image_versions}
do
  # Nexus3 does not use the 'library' namespace, so strip it from the destination namespace
  if [ "${namespace}" == "library" ]
  then
    namespace_dst=""
  else
    namespace_dst="${namespace}/"
  fi

  echo "Mirror ${namespace}/${image_name}:${version} to ${registry_dst}/${namespace_dst}${image_name}:${version}"

  url="https://${registry_dst}/v2/${namespace_dst}${image_name}/manifests/${version}"
  #if curl --insecure --output /dev/null --silent --fail "${url}"; then
  #  echo "Image already exists, skipping..."
  #  continue
  #fi

  echo "Pulling ${namespace}/${image_name}:${version}"
  docker pull ${namespace}/${image_name}:${version}
  docker tag ${namespace}/${image_name}:${version} ${registry_dst}/${namespace_dst}${image_name}:${version}

  echo "Pushing ${registry_dst}/${namespace_dst}${image_name}:${version}"
  docker push ${registry_dst}/${namespace_dst}${image_name}:${version}
done
