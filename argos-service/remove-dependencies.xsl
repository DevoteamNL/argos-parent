<?xml version="1.0" encoding="UTF-8"?>
<!--

    Argos Notary - A new way to secure the Software Supply Chain

    Copyright (C) 2019 - 2020 Rabobank Nederland
    Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pom="http://maven.apache.org/POM/4.0.0">
    <xsl:output method="xml" indent="no"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="//pom:dependencies" />
    <xsl:template match="//pom:dependencyManagement" />
</xsl:stylesheet>