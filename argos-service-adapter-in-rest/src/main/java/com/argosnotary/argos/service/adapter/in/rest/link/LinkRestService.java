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
package com.argosnotary.argos.service.adapter.in.rest.link;


import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.domain.permission.Permission;
import com.argosnotary.argos.service.adapter.in.rest.SignatureValidatorService;
import com.argosnotary.argos.service.adapter.in.rest.api.handler.LinkApi;
import com.argosnotary.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.service.domain.auditlog.AuditLog;
import com.argosnotary.argos.service.domain.auditlog.AuditParam;
import com.argosnotary.argos.service.domain.link.LinkMetaBlockRepository;
import com.argosnotary.argos.service.domain.security.LabelIdCheckParam;
import com.argosnotary.argos.service.domain.security.PermissionCheck;
import com.argosnotary.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static com.argosnotary.argos.service.adapter.in.rest.supplychain.SupplyChainLabelIdExtractor.SUPPLY_CHAIN_LABEL_ID_EXTRACTOR;
import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LinkRestService implements LinkApi {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    private final SupplyChainRepository supplyChainRepository;

    private final LinkMetaBlockMapper converter;

    private final SignatureValidatorService signatureValidatorService;

    @Override
    @PermissionCheck(permissions = Permission.LINK_ADD)
    @AuditLog
    @Transactional
    public ResponseEntity<Void> createLink(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR)
                                           @AuditParam("supplyChainId") String supplyChainId,
                                           @AuditParam(
                                                   value = "signature",
                                                   objectArgumentFilterBeanName = "auditLogSignatureArgumentFilter")
                                                   RestLinkMetaBlock restLinkMetaBlock) {
        log.info("createLink supplyChainId : {}", supplyChainId);
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        LinkMetaBlock linkMetaBlock = converter.convertFromRestLinkMetaBlock(restLinkMetaBlock);
        signatureValidatorService.validateSignature(linkMetaBlock.getLink(), linkMetaBlock.getSignature());
        linkMetaBlock.setSupplyChainId(supplyChainId);
        linkMetaBlockRepository.save(linkMetaBlock);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    @PermissionCheck(permissions = Permission.READ)
    public ResponseEntity<List<RestLinkMetaBlock>> findLink(@LabelIdCheckParam(dataExtractor = SUPPLY_CHAIN_LABEL_ID_EXTRACTOR) String supplyChainId, String optionalHash) {
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        return new ResponseEntity<>(Optional.ofNullable(optionalHash).map(hash -> linkMetaBlockRepository.findBySupplyChainAndSha(supplyChainId, hash))
                .orElseGet(() -> linkMetaBlockRepository.findBySupplyChainId(supplyChainId))
                .stream().map(converter::convertToRestLinkMetaBlock).collect(toList()), HttpStatus.OK);
    }

}
