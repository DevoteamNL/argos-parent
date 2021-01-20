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
