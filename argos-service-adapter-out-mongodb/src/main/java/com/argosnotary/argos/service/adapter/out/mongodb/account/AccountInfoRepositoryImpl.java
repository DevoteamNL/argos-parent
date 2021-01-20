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

import com.argosnotary.argos.domain.account.AccountInfo;
import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.domain.account.AccountType;
import com.argosnotary.argos.service.domain.account.AccountInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.MongoRegexCreator;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.MongoRegexCreator.MatchMode.CONTAINING;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountInfoRepositoryImpl implements AccountInfoRepository {
    protected static final String ACCOUNTS_KEYINFO_VIEW = "accounts-keyinfo";
    protected static final String ACCOUNTS_INFO_VIEW = "accounts-info";
    private static final String CASE_INSENSITIVE = "i";
    protected static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    protected static final String ACCOUNT_TYPE_FIELD = "accountType";
    private final MongoTemplate template;
    static final String ACCOUNT_KEY_ID_FIELD = "key.keyId";

    @Override
    public List<AccountKeyInfo> findByKeyIds(Set<String> keyIds) {
        Criteria rootCriteria = Criteria.where(ACCOUNT_KEY_ID_FIELD).in(keyIds);
        Query query = new Query(rootCriteria);
        return template.find(query, AccountKeyInfo.class, ACCOUNTS_KEYINFO_VIEW);
    }

    @Override
    public List<AccountInfo> findByNameIdPathToRootAndAccountType(String name, List<String> idPathToRoot, AccountType accountType) {
        Criteria criteria = where("name").regex(requireNonNull(MongoRegexCreator.INSTANCE.toRegularExpression(name, CONTAINING)), CASE_INSENSITIVE);
        criteria.orOperator(where(PARENT_LABEL_ID_FIELD).is(null), where(PARENT_LABEL_ID_FIELD).in(idPathToRoot));
        if (accountType != null) {
            criteria.and(ACCOUNT_TYPE_FIELD)
                    .is(accountType.name());
        }
        return template.find(new Query(criteria), AccountInfo.class, ACCOUNTS_INFO_VIEW);
    }
}
