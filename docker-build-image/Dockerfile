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

FROM openjdk:8-jdk-buster as jdk8

FROM maven:3.6.3-jdk-11

# jdk8 not available anymore
# install from image
RUN mkdir -p /usr/local/openjdk-8
COPY --from=jdk8 /usr/local/openjdk-8 /usr/local/openjdk-8

# install helm

ENV SHA256SUM 983c4f167060b3892a42f353c7891cabac36ec49f6042eae1046bd8a258b8a14
ENV HELM_VERSION 3.2.1

RUN wget "https://get.helm.sh/helm-v${HELM_VERSION}-linux-amd64.tar.gz" \
    && wget "https://get.helm.sh/helm-v3.2.1-linux-amd64.tar.gz.sha256sum" \
    && sha256sum -c "helm-v${HELM_VERSION}-linux-amd64.tar.gz.sha256sum" \
    && tar -xzf "helm-v${HELM_VERSION}-linux-amd64.tar.gz" \
    && mv linux-amd64/helm /usr/local/bin/helm \
    && rm -rf linux-amd64

COPY files/settings.xml /root/.m2/settings.xml
COPY files/toolchains.xml /root/.m2/toolchains.xml

# install docker-ce
RUN apt-get install -y curl        
RUN curl -fsSL https://get.docker.com | bash
