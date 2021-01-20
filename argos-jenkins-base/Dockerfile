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

FROM jenkins/jenkins:2.235.5-lts-alpine

USER root

RUN apk add --no-cache maven

COPY config/files/list-of-plugins.txt /plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /plugins.txt)

# groovy files
COPY config/files/*.groovy* /usr/share/jenkins/ref/init.groovy.d/

RUN cd /usr/share/jenkins/ref/plugins/ \
    && ln -s /drone/src/argos-jenkins-plugin/target/argos.hpi

