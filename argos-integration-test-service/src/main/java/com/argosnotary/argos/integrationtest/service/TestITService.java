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

import com.argosnotary.argos.domain.account.PersonalAccount;
import com.argosnotary.argos.domain.crypto.KeyPair;
import com.argosnotary.argos.domain.crypto.Signature;
import com.argosnotary.argos.domain.crypto.signing.JsonSigningSerializer;
import com.argosnotary.argos.domain.crypto.signing.Signer;
import com.argosnotary.argos.domain.layout.LayoutMetaBlock;
import com.argosnotary.argos.domain.link.LinkMetaBlock;
import com.argosnotary.argos.integrationtest.argos.service.api.handler.IntegrationTestServiceApi;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestKeyPair;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestLayoutMetaBlock;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestLinkMetaBlock;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestPersonalAccount;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestPersonalAccountWithToken;
import com.argosnotary.argos.integrationtest.argos.service.api.model.RestToken;
import com.argosnotary.argos.integrationtest.service.layout.LayoutMetaBlockMapper;
import com.argosnotary.argos.integrationtest.service.link.LinkMetaBlockMapper;
import com.argosnotary.argos.service.domain.account.AccountService;
import com.argosnotary.argos.service.domain.account.PersonalAccountRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.io.pem.PemGenerationException;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestMapping("/integration-test")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TestITService implements IntegrationTestServiceApi {

    protected static final String AZURE = "azure";
    @Value("${jwt.token.secret}")
    private String secret;

    private final RepositoryResetProvider repositoryResetProvider;

    private final LayoutMetaBlockMapper layoutMetaBlockMapper;

    private final LinkMetaBlockMapper linkMetaBlockMapper;

    private final AccountService accountService;

    private final PersonalAccountRepository personalAccountRepository;

    private SecretKey secretKey;

    private final AccountMapper accountMapper;
    private final MongoTemplate template;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }

    @Override
    public ResponseEntity<Void> resetDatabase() {
        log.info("resetDatabase");
        repositoryResetProvider.resetNotAllRepositories();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> resetDatabaseAll() {
        log.info("resetDatabaseAll");
        repositoryResetProvider.resetAllRepositories();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RestKeyPair> createKeyPair(String password) {
        KeyPair keyPair = null;
		try {
			keyPair = KeyPair.createKeyPair(password.toCharArray());
		} catch (NoSuchAlgorithmException | OperatorCreationException
				| PemGenerationException e) {
			log.error(e.getMessage());
		}
        assert keyPair != null;
        return ResponseEntity.ok(new RestKeyPair()
        		.keyId(keyPair.getKeyId())
        		.publicKey(keyPair.getPublicKey())
        		.encryptedPrivateKey(keyPair.getEncryptedPrivateKey()));
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> signLayout(String password, String keyId, RestLayoutMetaBlock restLayoutMetaBlock) {
        LayoutMetaBlock layoutMetaBlock = layoutMetaBlockMapper.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        KeyPair keyPair = getKeyPair(keyId);
        Signature signature = Signer.sign(keyPair, password.toCharArray(), new JsonSigningSerializer().serialize(layoutMetaBlock.getLayout()));
        List<Signature> signatures = new ArrayList<>(layoutMetaBlock.getSignatures());
        signatures.add(signature);
        layoutMetaBlock.setSignatures(signatures);
        return ResponseEntity.ok(layoutMetaBlockMapper.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    public ResponseEntity<RestLinkMetaBlock> signLink(String password, String keyId, RestLinkMetaBlock restLinkMetaBlock) {
        LinkMetaBlock linkMetaBlock = linkMetaBlockMapper.convertFromRestLinkMetaBlock(restLinkMetaBlock);

        KeyPair keyPair = getKeyPair(keyId);
        Signature signature = Signer.sign(keyPair, password.toCharArray(), new JsonSigningSerializer().serialize(linkMetaBlock.getLink()));
        linkMetaBlock.setSignature(signature);
        
        return ResponseEntity.ok(linkMetaBlockMapper.convertToRestLinkMetaBlock(linkMetaBlock));

    }

    @Override
    public ResponseEntity<String> auditLogGet() {
        List<Document> logs = template.findAll(Document.class, "auditlogs");
        String logsAsString = logs.stream().map(Document::toJson)
                .collect(Collectors.joining(","));
        return ResponseEntity.ok("[" + logsAsString + "]");
    }

    @Override
    public ResponseEntity<RestPersonalAccountWithToken> createPersonalAccount(RestPersonalAccount restPersonalAccount) {
        PersonalAccount personalAccount = PersonalAccount.builder()
                .name(restPersonalAccount.getName())
                .email(restPersonalAccount.getEmail())
                .providerName(AZURE)
                .providerId(UUID.randomUUID().toString())
                .roles(Collections.emptySet())
                .build();

        personalAccountRepository.save(personalAccount);

        RestPersonalAccountWithToken restPersonalAccountWithToken = accountMapper.map(personalAccount);
        restPersonalAccountWithToken.setToken(createToken(restPersonalAccountWithToken.getId(), new Date()));
        return ResponseEntity.ok(restPersonalAccountWithToken);
    }

    @Override
    public ResponseEntity<Void> deletePersonalAccount(String accountId) {
        repositoryResetProvider.deletePersonalAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<RestToken> createToken(String accountId, OffsetDateTime issuedAt) {
        log.info("issuedAt {}", issuedAt);
        return ResponseEntity.ok(new RestToken().token(createToken(accountId, Timestamp.valueOf(issuedAt.toLocalDateTime()))));
    }

    public String createToken(String accountId, Date issuedAt) {
        return Jwts.builder()
                .setSubject(accountId)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(issuedAt)
                .setExpiration(Timestamp.valueOf(LocalDateTime.now().plus(Duration.ofHours(12))))
                .signWith(secretKey)
                .compact();
    }

    private KeyPair getKeyPair(String keyId) {
    	return accountService.findKeyPairByKeyId(keyId).orElseThrow();
    }

}
