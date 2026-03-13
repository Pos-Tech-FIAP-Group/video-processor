package com.fiap.fiapx.auth.adapters.driven.infra.persistence.repository;

import com.fiap.fiapx.auth.adapters.driven.infra.persistence.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByUsername(String username);

    Optional<UserDocument> findByEmail(String email);

    Optional<UserDocument> findByUserUuid(String userUuid);
}
