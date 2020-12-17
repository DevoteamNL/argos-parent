/*
 * Copyright (C) 2020 Argos Notary
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.argosnotary.argos.service.adapter.out.mongodb.hierarchy;

import com.mongodb.client.result.UpdateResult;
import com.argosnotary.argos.domain.ArgosError;
import com.argosnotary.argos.domain.hierarchy.Label;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
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
public class LabelRepositoryImpl implements LabelRepository {

    public static final String COLLECTION = "labels";
    public static final String LABEL_ID_FIELD = "labelId";
    public static final String LABEL_NAME_FIELD = "name";
    public static final String PARENT_LABEL_ID_FIELD = "parentLabelId";
    private final MongoTemplate template;

    @Override
    public void save(Label label) {
        try {
            template.save(label, COLLECTION);
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(label, e);
        }
    }

    @Override
    public Optional<Label> findById(String id) {
        return Optional.ofNullable(template.findOne(getPrimaryKeyQuery(id), Label.class, COLLECTION));
    }

    @Override
    public boolean exists(String id) {
        return template.exists(getPrimaryKeyQuery(id), Label.class, COLLECTION);
    }

    @Override
    public void deleteById(String id) {
        template.remove(getPrimaryKeyQuery(id), COLLECTION);
    }

    @Override
    public Optional<Label> update(String id, Label label) {
        Query query = getPrimaryKeyQuery(id);
        Document document = new Document();
        template.getConverter().write(label, document);
        try {
            UpdateResult updateResult = template.updateFirst(query, Update.fromDocument(document), Label.class, COLLECTION);
            if (updateResult.getMatchedCount() > 0) {
                label.setLabelId(id);
                return Optional.of(label);
            } else {
                return Optional.empty();
            }
        } catch (DuplicateKeyException e) {
            throw duplicateKeyException(label, e);
        }
    }

    private Query getPrimaryKeyQuery(String id) {
        return new Query(Criteria.where(LABEL_ID_FIELD).is(id));
    }

    private ArgosError duplicateKeyException(Label label, DuplicateKeyException e) {
        return new ArgosError("label with name: " + label.getName() + " and parentLabelId: " + label.getParentLabelId() + " already exists", e, ArgosError.Level.WARNING);
    }

}
