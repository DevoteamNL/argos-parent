/*
 * Argos Notary - A new way to secure the Software Supply Chain
 *
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 * Copyright (C) 2019 - 2021 Gerard Borst <gerard.borst@argosnotary.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.argosnotary.argos.test;


import com.argosnotary.argos.argos4j.rest.api.model.RestKeyPair;
import com.argosnotary.argos.argos4j.rest.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.test.rest.api.model.TestKeyPair;
import com.argosnotary.argos.test.rest.api.model.TestLayoutMetaBlock;
import org.mapstruct.Mapper;

@Mapper
public interface RestMapper {

    RestLayoutMetaBlock mapTestLayout(TestLayoutMetaBlock layout);

    TestLayoutMetaBlock mapRestLayout(RestLayoutMetaBlock layout);

    RestKeyPair mapTestKeyPair(TestKeyPair keyPair);

    TestKeyPair mapRestKeyPair(RestKeyPair keyPair);

}
