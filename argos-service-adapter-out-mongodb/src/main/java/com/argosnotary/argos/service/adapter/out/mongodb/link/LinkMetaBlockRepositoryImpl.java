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
package com.argosnotary.argos.service.adapter.out.mongodb.link;

import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class LinkMetaBlockRepositoryImpl implements LinkMetaBlockRepository {

    public static final String COLLECTION = "linkMetaBlocks";
    public static final String SUPPLY_CHAIN_ID_FIELD = "supplyChainId";
    public static final String SEGMENT_NAME_FIELD = "link.layoutSegmentName";
    public static final String STEP_NAME_FIELD = "link.stepName";
    public static final String RUN_ID_FIELD = "link.runId";
    public static final String LINK_MATERIALS_HASH_FIELD = "link.materials.hash";
    public static final String LINK_PRODUCTS_HASH_FIELD = "link.products.hash";
    public static final String LINK_MATERIALS_URI_FIELD = "link.materials.uri";
    public static final String LINK_PRODUCTS_URI_FIELD = "link.products.uri";

    private final MongoTemplate template;

    @Override
    public List<LinkMetaBlock> findBySupplyChainId(String supplyChainId) {
        Query query = new Query(where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId));
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSha(String supplyChainId, String hash) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)
                .andOperator(
                        new Criteria()
                                .orOperator(
                                        new Criteria(LINK_MATERIALS_HASH_FIELD).is(hash),
                                        new Criteria(LINK_PRODUCTS_HASH_FIELD).is(hash)
                                )
                )
        );

        return template.find(query,LinkMetaBlock.class,COLLECTION);
    }

    @Override
    public void save(LinkMetaBlock link) {
        template.save(link, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(String supplyChainId, String segmentName, String stepName, List<String> hashes) {
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        hashes.forEach(hash -> andCriteria.add(Criteria.where(LINK_PRODUCTS_HASH_FIELD).is(hash)));
        rootCriteria.andOperator(andCriteria.toArray(new Criteria[0]));
        Query query = new Query(rootCriteria);
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(String supplyChainId, String segmentName, String stepName, List<String> hashes) {
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        hashes.forEach(hash -> andCriteria.add(Criteria.where(LINK_MATERIALS_HASH_FIELD).is(hash)));
        rootCriteria.andOperator(andCriteria.toArray(new Criteria[0]));
        Query query = new Query(rootCriteria);
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(
            String supplyChainId, String segmentName, String stepName, EnumMap<ArtifactType, Set<Artifact>> artifactTypeArtifacts) {
        if (artifactTypeArtifacts.isEmpty() || 
                (artifactTypeArtifacts.containsKey(ArtifactType.MATERIALS) && artifactTypeArtifacts.get(ArtifactType.MATERIALS).isEmpty()
                && artifactTypeArtifacts.containsKey(ArtifactType.PRODUCTS) && artifactTypeArtifacts.get(ArtifactType.PRODUCTS).isEmpty())) {
            List.of();
        }
        Criteria rootCriteria = Criteria.where(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId);
        List<Criteria> andCriteria = new ArrayList<>();
        andCriteria.add(Criteria.where(SEGMENT_NAME_FIELD).is(segmentName));
        andCriteria.add(Criteria.where(STEP_NAME_FIELD).is(stepName));
        if (artifactTypeArtifacts.containsKey(ArtifactType.MATERIALS)) {
            artifactTypeArtifacts.get(ArtifactType.MATERIALS).forEach(artifact -> andCriteria.add(
                    Criteria.where(LINK_MATERIALS_HASH_FIELD).is(artifact.getHash())
                    .and(LINK_MATERIALS_URI_FIELD).is(artifact.getUri())));
        }
        if (artifactTypeArtifacts.containsKey(ArtifactType.PRODUCTS)) {
            artifactTypeArtifacts.get(ArtifactType.PRODUCTS)
            .forEach(artifact -> andCriteria.add(
                    Criteria.where(LINK_PRODUCTS_HASH_FIELD).is(artifact.getHash())
                    .and(LINK_PRODUCTS_URI_FIELD).is(artifact.getUri())));
        }
        rootCriteria.andOperator(andCriteria.toArray(new Criteria[0]));
        Query query = new Query(rootCriteria);
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findByRunId(String supplyChainId, String runId) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)
                .andOperator(new Criteria(RUN_ID_FIELD).is(runId)));
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public List<LinkMetaBlock> findByRunId(String supplyChainId, String segmentName, String runId, Set<String> resolvedSteps) {
        Query query = new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD)
                .is(supplyChainId)
                .and(RUN_ID_FIELD).is(runId)
                .and(SEGMENT_NAME_FIELD).is(segmentName)
                .and(STEP_NAME_FIELD).nin(resolvedSteps)
        );
        return template.find(query, LinkMetaBlock.class, COLLECTION);
    }

    @Override
    public void deleteBySupplyChainId(String supplyChainId) {
        template.remove(new Query(new Criteria(SUPPLY_CHAIN_ID_FIELD).is(supplyChainId)), COLLECTION);
    }
}
