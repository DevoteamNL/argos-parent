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
package com.argosnotary.argos.integrationtest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RepositoryResetProviderImpl implements RepositoryResetProvider {

    private final MongoTemplate template;

    private static final Set<String> IGNORED_COLLECTIONS_FOR_ALL = Set.of("mongockChangeLog", "mongockLock", "hierarchy", "hierarchy_tmp", "system.views", "roles", "accounts-keyinfo", "accounts-info", "service-accounts-info-tmp", "service-accounts-key-info-tmp");
    private static final Set<String> IGNORED_COLLECTIONS = new HashSet<>();

    protected static final String PERSONALACCOUNTS = "personalaccounts";

    static {
        IGNORED_COLLECTIONS.addAll(IGNORED_COLLECTIONS_FOR_ALL);
        IGNORED_COLLECTIONS.add(PERSONALACCOUNTS);
        IGNORED_COLLECTIONS.add("labels");
        IGNORED_COLLECTIONS.add("serviceAccounts");
    }

    @Override
    public void resetAllRepositories() {
        template.getCollectionNames().stream()
                .filter(name -> !IGNORED_COLLECTIONS_FOR_ALL.contains(name))
                .forEach(name -> template.remove(new Query(), name));
    }

    @Override
    public void resetNotAllRepositories() {
        template.getCollectionNames().stream()
                .filter(name -> !IGNORED_COLLECTIONS.contains(name))
                .forEach(name -> template.remove(new Query(), name));
        template.remove(new Query(Criteria.where("email").nin("luke@skywalker.imp", "default@nl.nl", "default2@nl.nl")), PERSONALACCOUNTS);
        template.remove(new Query(Criteria.where("name").nin("default-root-label")), "labels");
        template.remove(new Query(Criteria.where("name").nin("default-sa1", "default-sa2", "default-sa3", "default-sa4", "default-sa5")), "serviceAccounts");
    }

    @Override
    public void deletePersonalAccount(String accountId) {
        template.remove(new Query(Criteria.where("accountId").is(accountId)), PERSONALACCOUNTS);
    }


}
