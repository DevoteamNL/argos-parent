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
package com.argosnotary.argos.service.domain.release;

import com.argosnotary.argos.domain.account.AccountKeyInfo;
import com.argosnotary.argos.domain.crypto.PublicKey;
import com.argosnotary.argos.domain.hierarchy.HierarchyMode;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.Artifact;
import com.argosnotary.argos.domain.release.ReleaseDossier;
import com.argosnotary.argos.domain.release.ReleaseDossierMetaData;
import com.argosnotary.argos.domain.release.ReleaseResult;
import com.argosnotary.argos.service.domain.NotFoundException;
import com.argosnotary.argos.service.domain.account.AccountInfoRepository;
import com.argosnotary.argos.service.domain.hierarchy.HierarchyRepository;
import com.argosnotary.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.verification.VerificationProvider;
import com.argosnotary.argos.service.domain.verification.VerificationRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.argosnotary.argos.domain.SupplyChainHelper.reversePath;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

    private final VerificationProvider verificationProvider;
    private final LayoutMetaBlockRepository layoutMetaBlockRepository;
    private final ReleaseRepository releaseRepository;
    private final AccountInfoRepository accountInfoRepository;
    private final HierarchyRepository hierarchyRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;

    @Override
    public ReleaseResult createRelease(String supplyChainId, List<Set<Artifact>> releaseArtifacts) {
        log.info("Release Artifacts [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);

        String supplyChainPath = getSupplyChainPath(supplyChainId);
        List<List<String>> releaseArtifactHashes = convertToReleaseArtifactHashes(releaseArtifacts);
        return releaseRepository
                .findReleaseByReleasedArtifactsAndPath(releaseArtifactHashes, supplyChainPath)
                .map(releaseDossierMetaData -> {
                    log.info("Artifacts already released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
                    return ReleaseResult
                        .builder()
                        .releaseIsValid(true)
                        .releaseDossierMetaData(releaseDossierMetaData)
                        .build();
                }
                )
                .orElseGet(() -> verifyAndStoreRelease(supplyChainId, releaseArtifacts, supplyChainPath, releaseArtifactHashes));
    }

    private ReleaseResult verifyAndStoreRelease(String supplyChainId, List<Set<Artifact>> releaseArtifacts, String supplyChainPath, List<List<String>> releaseArtifactHashes) {
        ReleaseResult.ReleaseResultBuilder releaseBuilder = ReleaseResult.builder();
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockRepository.findBySupplyChainId(supplyChainId);
        if (optionalLayoutMetaBlock.isPresent()) {

            Set<Artifact> allArtifacts = releaseArtifacts
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

            VerificationRunResult verificationRunResult = verificationProvider.verifyRun(optionalLayoutMetaBlock.get(), allArtifacts);
            releaseBuilder.releaseIsValid(verificationRunResult.isRunIsValid());

            if (verificationRunResult.isRunIsValid()) {
                ReleaseDossierMetaData releaseDossierMetaData = createAndStoreRelease(
                        supplyChainPath,
                        optionalLayoutMetaBlock.get(),
                        verificationRunResult,
                        releaseArtifactHashes);
                releaseBuilder.releaseDossierMetaData(releaseDossierMetaData);
                linkMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
            }
            log.info("Artifacts released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
            return releaseBuilder.build();
        }
        log.info("Artifacts release invalid [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
        return ReleaseResult.builder().releaseIsValid(false).build();
    }

    private ReleaseDossierMetaData createAndStoreRelease(String supplyChainPath, LayoutMetaBlock layoutMetaBlock,
                                                         VerificationRunResult verificationRunResult,
                                                         List<List<String>> releaseArtifacts) {

        List<ReleaseDossier.Account> accounts = getAccounts(layoutMetaBlock);

        ReleaseDossierMetaData releaseDossierMetaData = ReleaseDossierMetaData.builder()
                .releaseArtifacts(releaseArtifacts)
                .supplyChainPath(supplyChainPath)
                .build();

        ReleaseDossier releaseDossier = ReleaseDossier.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(verificationRunResult.getValidLinkMetaBlocks())
                .accounts(accounts)
                .build();
        releaseRepository.storeRelease(releaseDossierMetaData, releaseDossier);
        return releaseDossierMetaData;
    }

    private List<List<String>> convertToReleaseArtifactHashes(List<Set<Artifact>> releaseArtifacts) {
        return releaseArtifacts
                .stream()
                .map(s -> s.stream()
                        .map(Artifact::getHash)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private String getSupplyChainPath(String supplyChainId) {
        return hierarchyRepository.getSubTree(supplyChainId, HierarchyMode.NONE, 0)
                .map(treeNode -> String.join(".", reversePath(treeNode.getPathToRoot())) + "." + treeNode.getName())
                .orElseThrow(() -> new NotFoundException("Supplychain not found"));
    }

    private List<ReleaseDossier.Account> getAccounts(LayoutMetaBlock layoutMetaBlock) {
        Set<String> keyIds = layoutMetaBlock
                .getLayout()
                .getKeys()
                .stream()
                .map(PublicKey::getKeyId)
                .collect(Collectors.toSet());

        return accountInfoRepository.findByKeyIds(keyIds).stream()
                .map(toAccount()
                ).collect(Collectors.toList());
    }

    private static Function<AccountKeyInfo, ReleaseDossier.Account> toAccount() {
        return acountInfo ->
                ReleaseDossier
                        .Account
                        .builder()
                        .id(acountInfo.getAccountId())
                        .keyId(acountInfo.getKey().getKeyId())
                        .name(acountInfo.getName())
                        .path(String.join(".", reversePath(acountInfo.getPathToRoot())))
                        .type(acountInfo.getAccountType()).build();
    }
}
