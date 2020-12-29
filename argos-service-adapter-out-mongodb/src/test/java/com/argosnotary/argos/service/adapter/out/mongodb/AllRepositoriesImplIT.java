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
package com.argosnotary.argos.service.adapter.out.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.account.ServiceAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.hierarchy.Label;
import com.argosnotary.argos.domain.hierarchy.TreeNode;
import com.argosnotary.argos.domain.layout.ApprovalConfiguration;
import com.argosnotary.argos.domain.layout.ArtifactType;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.link.Link;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.supplychain.SupplyChain;
import com.argosnotary.argos.service.adapter.out.mongodb.account.PersonalAccountRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.account.ServiceAccountRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.HierarchyRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.hierarchy.LabelRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.layout.ApprovalConfigurationRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.link.LinkMetaBlockRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.release.DateToOffsetTimeConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.DocumentToReleaseDossierMetaDataConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.OffsetTimeToDateConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseDossierMetaDataToDocumentConverter;
import com.argosnotary.argos.service.adapter.out.mongodb.release.ReleaseRepositoryImpl;
import com.argosnotary.argos.service.adapter.out.mongodb.supplychain.SupplyChainRepositoryImpl;
import com.argosnotary.argos.service.domain.account.AccountSearchParams;
import com.argosnotary.argos.service.domain.account.PersonalAccountRepository;
import com.argosnotary.argos.service.domain.account.ServiceAccountRepository;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.hierarchy.LabelRepository;
import com.argosnotary.argos.service.domain.layout.ApprovalConfigurationRepository;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.release.ReleaseRepository;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;

import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/*
 * Integration tests of all repositories exept the Release Repository
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllRepositoriesImplIT {
    private static final String SEGMENT_NAME = "segmentName";
    private static final String STEP_NAME = "stepName";
    private static final String SUPPLY_CHAIN_ID = "supplyChainId";
    private static final String SUPPLYCHAIN = "supplychain";
    private static final String HASH_1 = "74a88c1cb96211a8f648af3509a1207b2d4a15c0202cfaa10abad8cc26300c63";
    private static final String HASH_2 = "1e6a4129c8b90e9b6c4727a59b1013d714576066ad1bad05034847f30ffb62b6";
    private static final String ARGOS_TEST_IML = "argos-test.iml";
    private static final String DOCKER_1_IML = "docker (1).iml";
    public static final String RUN_ID = "runId";
    private static final String PIETJE = "Pietje";
    private static final PersonalAccount PIETJE_ACCOUNT = PersonalAccount.builder().name(PIETJE).email("pietje@piet.nl")
            .activeKeyPair(new KeyPair("keyId1", null, null))
            .inactiveKeyPairs(Set.of(
                    new KeyPair("keyId2", null, null),
                    new KeyPair("keyId3", null, null))).build();
    private static final String KLAASJE = "Klaasje";
    private static final PersonalAccount KLAASJE_ACCOUNT = PersonalAccount.builder()
            .name(KLAASJE).email("klaasje@klaas.nl")
            .activeKeyPair(new KeyPair("keyId4", null, null))
            .inactiveKeyPairs(
                    Set.of(new KeyPair("keyId5", null, null),
                    new KeyPair("keyId6", null, null)))
            .build();
    public static final String ID_FIELD = "_id";
    public static final String METADATA_RELEASE_ARTIFACTS_ARTIFACTS_HASH_FIELD = "metadata.releaseArtifacts.artifactsHash";
    public static final String METADATA_RELEASE_ARTIFACTS_FIELD = "metadata.releaseArtifacts";
    public static final String METADATA_SUPPLY_CHAIN_PATH_FIELD = "metadata.supplyChainPath";
    public static final String COLLECTION_NAME = "fs.files";
    public static final String RELEASE_ARTIFACTS_FIELD = "releaseArtifacts";
    public static final String ARTIFACTS_HASH = "artifactsHash";
    public static final String HASHES = "hashes";
    public static final String SUPPLY_CHAIN_PATH_FIELD = "supplyChainPath";
    public static final String RELEASE_DATE_FIELD = "releaseDate";
    public static final String METADATA_FIELD = "metadata";
    
    protected static final List<List<String>> RELEASE_ARTIFACTS = List.of(List.of("hash1-1", "hash1-2"), List.of("hash2-1", "hash2-2"));
    
    private PersonalAccountRepository personalAccountRepository;

    
    private LinkMetaBlockRepository linkMetaBlockRepository;
    
    private HierarchyRepository hierarchyRepository;
    private LabelRepository labelRepository;
    private SupplyChainRepository supplyChainRepository;
    private ServiceAccountRepository serviceAccountRepository;
    private ApprovalConfigurationRepository approvalConfigurationRepository;
    private ReleaseRepository releaseRepository;
    
    private GridFsTemplate gridFsTemplate;

    private MongoTestBase mongoTestBase;

    @BeforeAll
    void setup() throws IOException {
        mongoTestBase = new MongoTestBase();
        MongoTemplate mongoTemplate = mongoTestBase.startDb(MongoConfig.CHANGELOG_SCAN_PACKAGE);
        hierarchyRepository = new HierarchyRepositoryImpl(mongoTemplate);
        labelRepository = new LabelRepositoryImpl(mongoTemplate);
        supplyChainRepository = new SupplyChainRepositoryImpl(mongoTemplate);
        serviceAccountRepository = new ServiceAccountRepositoryImpl(mongoTemplate);
        approvalConfigurationRepository = new ApprovalConfigurationRepositoryImpl(mongoTemplate);
        linkMetaBlockRepository = new LinkMetaBlockRepositoryImpl(mongoTemplate);
        personalAccountRepository = new PersonalAccountRepositoryImpl(mongoTemplate);
        MongoDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(MongoClients.create(mongoTestBase.getConnectionString()), "test");
        gridFsTemplate = new GridFsTemplate(factory, getDefaultMongoConverter(factory)); 
        ObjectMapper mapper = new ObjectMapper();
        releaseRepository = new ReleaseRepositoryImpl(gridFsTemplate, mongoTemplate, mapper);
        createDataSet();
    }

    @AfterAll
    void clean() {
        mongoTestBase.stopDb();
    }
    
    @Test
    void saveAllApprovalConfigurations() {
        approvalConfigurationRepository.saveAll(SUPPLY_CHAIN_ID, List.of(ApprovalConfiguration
                .builder()
                .segmentName("segment2")
                .stepName("step2")
                .supplyChainId(SUPPLY_CHAIN_ID)
                .build()));

        approvalConfigurationRepository.saveAll(SUPPLY_CHAIN_ID, List.of(ApprovalConfiguration
                .builder()
                .segmentName(SEGMENT_NAME)
                .stepName(STEP_NAME)
                .supplyChainId("otherSupplyChainId")
                .build()));

        approvalConfigurationRepository.saveAll(SUPPLY_CHAIN_ID, List.of(ApprovalConfiguration
                .builder()
                .segmentName("new segment name")
                .stepName(STEP_NAME)
                .supplyChainId(SUPPLY_CHAIN_ID)
                .build()));
        assertThat(approvalConfigurationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID), hasSize(1));
        assertThat(approvalConfigurationRepository.findBySupplyChainId(SUPPLY_CHAIN_ID).iterator().next().getSegmentName(), is("new segment name"));
        assertThat(approvalConfigurationRepository.findBySupplyChainId("otherSupplyChainId"), hasSize(1));
        
    }

    @Test
    void testGetRootNodes() {
        List<TreeNode> result = hierarchyRepository.getRootNodes(HierarchyMode.NONE, 0);
        assertThat(result, hasSize(1));
        assertThat(result.iterator().next().getName(), is("nl"));
        assertThat(result.iterator().next().isHasChildren(), is(true));
    }

    @Test
    void testFindByNamePathToRootAndType() {
        List<String> pathToRoot = List.of("team 1", "department 1", "company 1", "nl");
        Optional<TreeNode> optionalSubTree = hierarchyRepository
                .findByNamePathToRootAndType("team 1 supply chain", pathToRoot, TreeNode.Type.SUPPLY_CHAIN);
        assertThat(optionalSubTree.isPresent(), is(true));
        assertThat(optionalSubTree.get().getName(), is("team 1 supply chain"));
        assertThat(optionalSubTree.get().getType(), is(TreeNode.Type.SUPPLY_CHAIN));
    }

    @Test
    void testGetSubTree() {
        List<TreeNode> result = hierarchyRepository.getRootNodes(HierarchyMode.NONE, 0);
        String referenceId = result.iterator().next().getReferenceId();
        Optional<TreeNode> optionalSubTree = hierarchyRepository.getSubTree(referenceId, HierarchyMode.ALL, 0);
        assertThat(optionalSubTree.isPresent(), is(true));
        TreeNode treeNode = optionalSubTree.get();
        assertThat(treeNode.getChildren(), hasSize(1));
        TreeNode company1 = treeNode.getChildren().iterator().next();
        assertThat(company1.getName(), is("company 1"));
        assertThat(company1.getChildren(), hasSize(1));
        TreeNode department1 = company1.getChildren().iterator().next();
        assertThat(department1.getChildren(), hasSize(3));
        TreeNode team1 = department1.getChildren().iterator().next();
        assertThat(team1.getName(), is("team 1"));
        assertThat(team1.getChildren(), hasSize(3));
        TreeNode sa = team1.getChildren().iterator().next();
        assertThat(sa.getName(), is("team 1 sa 1"));
        assertThat(sa.getIdPathToRoot().toArray(), arrayContaining(List.of(
                team1.getReferenceId(),
                department1.getReferenceId(),
                company1.getReferenceId(),
                treeNode.getReferenceId()
                ).toArray())
        );
        assertThat(sa.getType(), is(TreeNode.Type.SERVICE_ACCOUNT));
    }
    
    @Test
    void findByRunIdShouldRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findByRunId(SUPPLYCHAIN, RUN_ID);
        assertThat(links, hasSize(1));
    }

    @Test
    void findByRunIdWithSegmentNameAndResolvedStepShouldNotRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findByRunId(SUPPLYCHAIN, SEGMENT_NAME, RUN_ID, singleton(STEP_NAME));
        assertThat(links, hasSize(0));
    }

    @Test
    void findByRunIdWithSegmentNameShouldRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findByRunId(SUPPLYCHAIN, SEGMENT_NAME, RUN_ID, new HashSet<>());
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndProductHashesShouldRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndMultipleProductHashesShouldRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, HASH_2));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndProductHashesShouldNotRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, "INCORRECT_HASH"));
        assertThat(links, hasSize(0));
    }


    @Test
    void findBySupplyChainAndStepNameAndMaterialsHashesShouldRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndMultipleMaterialsHashesShouldRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, HASH_2));
        assertThat(links, hasSize(1));
    }

    @Test
    void findBySupplyChainAndStepNameAndMaterialsHashesShouldNotRetreive() {
        List<LinkMetaBlock> links = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, List.of(HASH_1, "INCORRECT_HASH"));
        assertThat(links, hasSize(0));
    }

    @Test
    void findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashesShouldRetrieve() {
        EnumMap<ArtifactType, Set<Artifact>> artifactMap = new EnumMap<>(ArtifactType.class);
        artifactMap.put(ArtifactType.MATERIALS, new HashSet<>());
        artifactMap.get(ArtifactType.MATERIALS).addAll(createMaterials());
        artifactMap.put(ArtifactType.PRODUCTS, new HashSet<>());
        artifactMap.get(ArtifactType.PRODUCTS).addAll(createProducts());
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, artifactMap);
        assertThat(blocks, hasSize(1));
    }

    @Test
    void findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashesShouldNotRetrieve() {
        EnumMap<ArtifactType, Set<Artifact>> artifactMap = new EnumMap<>(ArtifactType.class);
        artifactMap.put(ArtifactType.MATERIALS, new HashSet<>());
        artifactMap.get(ArtifactType.MATERIALS).addAll(createMaterials());
        artifactMap.put(ArtifactType.PRODUCTS, new HashSet<>());
        artifactMap.get(ArtifactType.PRODUCTS).addAll(createProducts());
        artifactMap.get(ArtifactType.PRODUCTS).add(new Artifact("file1", "hash1"));
        List<LinkMetaBlock> blocks = linkMetaBlockRepository.findBySupplyChainAndSegmentNameAndStepNameAndArtifactTypesAndArtifactHashes(SUPPLYCHAIN, SEGMENT_NAME, STEP_NAME, artifactMap);
        assertThat(blocks, hasSize(0));
    }
    
    @Test
    void searchByName() {
        assertThat(searchByName("tje"), contains(PIETJE));
        assertThat(searchByName("je"), contains(KLAASJE, PIETJE));
        assertThat(searchByName("J"), contains(KLAASJE, PIETJE));
        assertThat(searchByName("Klaa"), contains(KLAASJE));
        assertThat(searchByName("klaasje"), contains(KLAASJE));
        assertThat(searchByName("Pietje"), contains(PIETJE));
        assertThat(searchByName("z"), empty());
    }

    @Test
    void searchByActiveIds() {
        assertThat(searchActiveIds(List.of("keyId4", "keyId1")), contains(KLAASJE, PIETJE));
        assertThat(searchActiveIds(List.of("keyId4", "otherKey")), contains(KLAASJE));
        assertThat(searchActiveIds(List.of("otherKey", "keyId1")), contains(PIETJE));
        assertThat(searchActiveIds(List.of("otherKey", "keyId2")), empty());
    }

    @Test
    void searchByInActiveIds() {
        assertThat(searchInActiveIds(List.of("keyId1", "keyId2", "keyId3", "keyId4", "keyId5", "keyId6", "other")), contains(KLAASJE, PIETJE));
        assertThat(searchInActiveIds(List.of("keyId6", "otherKey")), contains(KLAASJE));
        assertThat(searchInActiveIds(List.of("otherKey", "keyId2")), contains(PIETJE));
        assertThat(searchInActiveIds(List.of("keyId3")), contains(PIETJE));
        assertThat(searchInActiveIds(List.of("keyId5")), contains(KLAASJE));
        assertThat(searchInActiveIds(List.of("otherKey", "keyId1")), empty());
    }
    
    @Test
    void storeReleaseAndRetrieval() {
        ReleaseDossierMetaData stored = storeReleaseDossier();
        assertThat(stored.getDocumentId(), is(IsNull.notNullValue()));
        assertThat(stored.getReleaseDate(), is(IsNull.notNullValue()));
        Optional<String> storedFile = releaseRepository.getRawReleaseFileById(stored.getDocumentId());
        assertThat(storedFile.isEmpty(), is(false));
        gridFsTemplate.delete(new Query());
    }

    private ReleaseDossierMetaData storeReleaseDossier() {
        LayoutMetaBlock layoutMetaBlock = LayoutMetaBlock.builder().supplyChainId("supplychain").build();
        ReleaseDossier releaseDossier = ReleaseDossier.builder().layoutMetaBlock(layoutMetaBlock).build();
        ReleaseDossierMetaData releaseDossierMetaData = ReleaseDossierMetaData.builder()
                .releaseArtifacts(RELEASE_ARTIFACTS)
                .supplyChainPath("path.to.supplychain").build();

        return releaseRepository.storeRelease(releaseDossierMetaData, releaseDossier);
    }


    @Test
    void findReleaseByReleasedArtifactsAndPath() {
        storeReleaseDossier();
        Optional<ReleaseDossierMetaData> dossierMetaData = releaseRepository
                .findReleaseByReleasedArtifactsAndPath(RELEASE_ARTIFACTS, null);
        assertThat(dossierMetaData.isPresent(), is(true));

        Optional<ReleaseDossierMetaData> emptyDossier = releaseRepository
                .findReleaseByReleasedArtifactsAndPath(List.of(List.of("hash1-incorrect", "hash1-2"), List.of("hash2-1", "hash2-2")), null);

        assertThat(emptyDossier.isPresent(), is(false));
        gridFsTemplate.delete(new Query());
    }

    @Test
    void artifactsAreReleased() {
        storeReleaseDossier();
        boolean artifactsAreReleased = releaseRepository
                .artifactsAreReleased(RELEASE_ARTIFACTS.get(0), List.of("path.to"));
        assertThat(artifactsAreReleased, is(true));
        gridFsTemplate.delete(new Query());
    }

    void createDataSet() {
        Label root = createLabel("nl", null);
        Label company1 = createLabel("company 1", root.getLabelId());
        Label department1 = createLabel("department 1", company1.getLabelId());
        createLabel("team 3", department1.getLabelId());
        createLabel("team 2", department1.getLabelId());
        Label team1 = createLabel("team 1", department1.getLabelId());
        createLabel("team 1 supply chain", team1.getLabelId());
        createServiceAccount("team 1 sa 1", team1.getLabelId());
        createSupplyChain("team 1 supply chain", team1.getLabelId());

        approvalConfigurationRepository.saveAll(SUPPLY_CHAIN_ID, List.of(ApprovalConfiguration
                .builder()
                .segmentName(SEGMENT_NAME)
                .stepName(STEP_NAME)
                .supplyChainId(SUPPLY_CHAIN_ID)
                .build()));
        LinkMetaBlock linkmetaBlock = LinkMetaBlock
                .builder()
                .supplyChainId(SUPPLYCHAIN)
                .signature(createSignature())
                .link(createLink())
                .build();
        linkMetaBlockRepository.save(linkmetaBlock);
        personalAccountRepository.save(PIETJE_ACCOUNT);
        personalAccountRepository.save(KLAASJE_ACCOUNT);
    }
    
    private static MongoConverter getDefaultMongoConverter(MongoDatabaseFactory factory) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
        MongoCustomConversions conversions = new MongoCustomConversions(
                List.of(
                        new DateToOffsetTimeConverter(), 
                        new OffsetTimeToDateConverter(),
                        new ReleaseDossierMetaDataToDocumentConverter(),
                        new DocumentToReleaseDossierMetaDataConverter()));
        MongoMappingContext mappingContext = new MongoMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        mappingContext.afterPropertiesSet();
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mappingContext);
        converter.setCustomConversions(conversions);
        converter.setCodecRegistryProvider(factory);
        converter.afterPropertiesSet();
        return converter;
    }

    private void createServiceAccount(String name, String parentLabelId) {
        serviceAccountRepository.save(ServiceAccount.builder().name(name).parentLabelId(parentLabelId).build());
    }

    private Label createLabel(String name, String parentId) {
        Label label = Label.builder()
                .name(name)
                .parentLabelId(parentId)
                .build();
        labelRepository.save(label);
        return label;
    }

    private SupplyChain createSupplyChain(String name, String parentId) {
        SupplyChain supplyChain = SupplyChain.builder()
                .name(name)
                .parentLabelId(parentId)
                .build();
        supplyChainRepository.save(supplyChain);
        return supplyChain;
    }
    
    private Signature createSignature() {
        return Signature.builder()
                .keyId("2392017103413adf6fa3b535e3714b30bc0a901229d0e76784f5ffca653f905e")
                .signature("signature")
                .build();
    }

    private Link createLink() {
        return Link
                .builder()
                .runId(RUN_ID)
                .layoutSegmentName(SEGMENT_NAME)
                .stepName(STEP_NAME)
                .materials(createMaterials())
                .products(createProducts())
                .build();
    }

    private List<Artifact> createMaterials() {
        return asList(

                Artifact.builder()
                        .hash(HASH_1)
                        .uri(ARGOS_TEST_IML)
                        .build(),

                Artifact.builder()
                        .hash(HASH_2)
                        .uri(DOCKER_1_IML)
                        .build());
    }

    private List<Artifact> createProducts() {
        return asList(

                Artifact.builder()
                        .hash(HASH_1)
                        .uri(ARGOS_TEST_IML)
                        .build(),

                Artifact.builder()
                        .hash(HASH_2)
                        .uri(DOCKER_1_IML)
                        .build());
    }
    
    private List<String> searchByName(String name) {
        return searchAccount(AccountSearchParams.builder().name(name).build());
    }

    private List<String> searchActiveIds(List<String> activeIds) {
        return searchAccount(AccountSearchParams.builder().activeKeyIds(activeIds).build());
    }

    private List<String> searchInActiveIds(List<String> inActiveIds) {
        return searchAccount(AccountSearchParams.builder().inActiveKeyIds(inActiveIds).build());
    }

    private List<String> searchAccount(AccountSearchParams params) {
        return personalAccountRepository.search(params).stream().map(PersonalAccount::getName).collect(Collectors.toList());
    }
}