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
package com.argosnotary.argos.service.adapter.out.mongodb.account;

import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.service.domain.account.ServiceAccountRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ServiceAccountRepositoryImpl implements ServiceAccountRepository {

    public static final String COLLECTION = "serviceAccounts";
    public static final String ACCOUNT_ID_FIELD = "accountId";
    public static final String ACCOUNT_NAME_FIELD = "name";
    public static final String ACTIVE_KEY_ID_FIELD = "activeKeyPair.keyId";
    public static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    private final MongoTemplate template;

    @Override
    public void save(ServiceAccount account) {
        try {
            template.save(account, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(account, e);
        }
    }

    @Override
    public Optional<ServiceAccount> findById(String id) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(id), ServiceAccount.class, COLLECTION));
    }

    @Override
    public Optional<ServiceAccount> findByActiveKeyId(String activeKeyId) {
        return Optional.ofNullable(template.findOne(getActiveKeyQuery(activeKeyId), ServiceAccount.class, COLLECTION));
    }

    @Override
    public void update(ServiceAccount account) {
        Query query = getPrimaryKeyQuery(account.getAccountId());
        Document document = new Document();
        template.getConverter().write(account, document);
        try {
            template.updateFirst(query, Update.fromDocument(document), ServiceAccount.class, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(account, e);
        }
    }

    @Override
    public void delete(String accountId) {
        template.remove(getPrimaryKeyQuery(accountId), COLLECTION);
    }

    @Override
    public boolean activeKeyExists(String activeKeyId) {
        return template.exists(getActiveKeyQuery(activeKeyId), ServiceAccount.class, COLLECTION);
    }

    @Override
    public Optional<String> findParentLabelIdByAccountId(String accountId) {
        Query query = getPrimaryKeyQuery(accountId);
        query.fields().include(PARENT_LABEL_ID_FIELD);
        return Optional.ofNullable(template.findOne(query, ServiceAccount.class, COLLECTION)).map(ServiceAccount::getParentLabelId);
    }

    @Override
    public boolean exists(String serviceAccountId) {
        return template.exists(getPrimaryKeyQuery(serviceAccountId), COLLECTION);
    }

    private Query getActiveKeyQuery(String activekeyId) {
        return new Query(Criteria.where(ACTIVE_KEY_ID_FIELD).is(activekeyId));
    }

    private Query getPrimaryKeyQuery(String id) {
        return new Query(Criteria.where(ACCOUNT_ID_FIELD).is(id));
    }

    private ArgosError duplicateKeyException(ServiceAccount account, DuplicateKeyException e) {
        return new ArgosError("service account with name: " + account.getName() + " and parentLabelId: " + account.getParentLabelId() + " already exists",
                e, ArgosError.Level.WARNING);
    }

}
