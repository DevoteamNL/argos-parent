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

import com.mongodb.client.result.UpdateResult;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.permission.Role;
import com.argosnotary.argos.service.domain.account.AccountSearchParams;
import org.bson.Document;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

import static com.argosnotary.argos.service.adapter.out.mongodb.account.PersonalAccountRepositoryImpl.COLLECTION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalAccountRepositoryImplTest {


    private static final String ACTIVE_KEY_ID = "activeKeyId";
    private static final long COUNT = 12334L;
    private static final String NAME = "name";
    @Mock
    private MongoTemplate template;

    private PersonalAccountRepositoryImpl repository;

    @Mock
    private PersonalAccount personalAccount;

    @Captor
    private ArgumentCaptor<Query> queryArgumentCaptor;

    @Mock
    private MongoConverter converter;

    @Mock
    private UpdateResult updateResult;

    @Captor
    private ArgumentCaptor<Update> updateArgumentCaptor;

    @BeforeEach
    void setUp() {
        repository = new PersonalAccountRepositoryImpl(template);
    }

    @Test
    void saveShouldUser() {
        repository.save(personalAccount);
        verify(template).save(personalAccount, COLLECTION);
    }

    @Test
    void findByUserId() {
        when(template.findOne(any(), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(personalAccount);
        assertThat(repository.findByAccountId("userId"), is(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"accountId\" : \"userId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByEmail() {
        when(template.findOne(any(), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(personalAccount);
        assertThat(repository.findByEmail("email"), is(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"email\" : \"email\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void update() {
        when(personalAccount.getAccountId()).thenReturn("userId");
        when(template.getConverter()).thenReturn(converter);
        when(template.updateFirst(any(Query.class), any(Update.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(updateResult);
        repository.update(personalAccount);
        verify(template).updateFirst(queryArgumentCaptor.capture(), updateArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), is("Query: { \"accountId\" : \"userId\"}, Fields: {}, Sort: {}"));
        assertThat(updateArgumentCaptor.getValue().toString(), is("{}"));
        verify(converter).write(eq(personalAccount), any(Document.class));
    }

    @Test
    void activeKeyExists() {
        when(template.exists(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(true);
        assertThat(repository.activeKeyExists(ACTIVE_KEY_ID), Matchers.is(true));
        verify(template).exists(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"activeKeyPair.keyId\" : \"activeKeyId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void findByActiveKeyId() {
        when(template.findOne(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(personalAccount);
        assertThat(repository.findByActiveKeyId(ACTIVE_KEY_ID), equalTo(Optional.of(personalAccount)));
        verify(template).findOne(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"activeKeyPair.keyId\" : \"activeKeyId\"}, Fields: {}, Sort: {}"));
    }

    @Test
    void getTotalNumberOfAccounts() {
        when(template.count(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(COUNT);
        assertThat(repository.getTotalNumberOfAccounts(), equalTo(COUNT));
        verify(template).count(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: {}, Fields: {}, Sort: {}"));
    }


    @Test
    void searchAll() {
        when(template.find(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(List.of(personalAccount));
        assertThat(repository.search(AccountSearchParams.builder().build()), contains(personalAccount));
        verify(template).find(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: {}, Fields: { \"accountId\" : 1, \"name\" : 1, \"email\" : 1}, Sort: { \"name\" : 1}"));
    }

    @Test
    void searchByRole() {
        when(template.find(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(List.of(personalAccount));
        assertThat(repository.searchWithRoles(AccountSearchParams.builder().role(Role.ADMINISTRATOR).build()), contains(personalAccount));
        verify(template).find(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"roles\" : { \"$in\" : [\"ADMINISTRATOR\"]}}, Fields: { \"accountId\" : 1, \"roles\" : 1, \"name\" : 1, \"email\" : 1}, Sort: { \"name\" : 1}"));
    }

    @Test
    void searchByLocalPermissionsLabelId() {
        when(template.find(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(List.of(personalAccount));
        assertThat(repository.search(AccountSearchParams.builder().name(NAME).build()), contains(personalAccount));
        verify(template).find(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"name\" : { \"$regularExpression\" : { \"pattern\" : \".*name.*\", \"options\" : \"i\"}}}, Fields: { \"accountId\" : 1, \"name\" : 1, \"email\" : 1}, Sort: { \"name\" : 1}"));
    }

    @Test
    void searchByActiveKeyIds() {
        when(template.find(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(List.of(personalAccount));
        assertThat(repository.search(AccountSearchParams.builder().activeKeyIds(List.of("key1", "key2")).build()), contains(personalAccount));
        verify(template).find(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"activeKeyPair.keyId\" : { \"$in\" : [\"key1\", \"key2\"]}}, Fields: { \"accountId\" : 1, \"name\" : 1, \"email\" : 1}, Sort: { \"name\" : 1}"));
    }

    @Test
    void searchByInActiveKeyIds() {
        when(template.find(any(Query.class), eq(PersonalAccount.class), eq(COLLECTION))).thenReturn(List.of(personalAccount));
        assertThat(repository.search(AccountSearchParams.builder().inActiveKeyIds(List.of("key1", "key2")).build()), contains(personalAccount));
        verify(template).find(queryArgumentCaptor.capture(), eq(PersonalAccount.class), eq(COLLECTION));
        assertThat(queryArgumentCaptor.getValue().toString(), Matchers.is("Query: { \"inactiveKeyPairs.keyId\" : { \"$in\" : [\"key1\", \"key2\"]}}, Fields: { \"accountId\" : 1, \"name\" : 1, \"email\" : 1}, Sort: { \"name\" : 1}"));
    }
}
