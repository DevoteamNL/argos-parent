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