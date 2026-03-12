package com.fiap.fiapx.auth.adapters.driven.infra.persistence.adapter;

import com.fiap.fiapx.auth.adapters.driven.infra.persistence.entity.UserDocument;
import com.fiap.fiapx.auth.adapters.driven.infra.persistence.repository.UserMongoRepository;
import com.fiap.fiapx.auth.core.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserPersistenceAdapter")
class UserPersistenceAdapterTest {

    @Mock
    private UserMongoRepository repository;

    private UserPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserPersistenceAdapter(repository);
    }

    @Nested
    @DisplayName("toUser com documento roles/userUuid null")
    class DocumentoComRolesEUuidNull {

        @Test
        @DisplayName("findByUsername com doc roles null e userUuid null → User com roles vazias e userUuid null")
        void findByUsername_doc_com_nulls_retorna_user_corrigido() {
            UserDocument doc = new UserDocument();
            doc.setId("id1");
            doc.setUserUuid(null);
            doc.setUsername("u1");
            doc.setEmail("u1@example.com");
            doc.setPasswordHash("hash");
            doc.setEnabled(true);
            doc.setCreatedAt(LocalDateTime.now());
            doc.setRoles(null);

            when(repository.findByUsername("u1")).thenReturn(Optional.of(doc));

            Optional<User> result = adapter.findByUsername("u1");

            assertThat(result).isPresent();
            User user = result.get();
            assertThat(user.getRoles()).isNotNull().isEmpty();
            assertThat(user.getUserUuid()).isNull();
            assertThat(user.getId()).isEqualTo("id1");
            assertThat(user.getUsername()).isEqualTo("u1");
        }

        @Test
        @DisplayName("findById com doc roles null e userUuid null → User com roles vazias e userUuid null")
        void findById_doc_com_nulls_retorna_user_corrigido() {
            UserDocument doc = new UserDocument();
            doc.setId("id2");
            doc.setUserUuid(null);
            doc.setUsername("u2");
            doc.setEmail("u2@example.com");
            doc.setPasswordHash("hash");
            doc.setEnabled(true);
            doc.setCreatedAt(LocalDateTime.now());
            doc.setRoles(null);

            when(repository.findById("id2")).thenReturn(Optional.of(doc));

            Optional<User> result = adapter.findById("id2");

            assertThat(result).isPresent();
            User user = result.get();
            assertThat(user.getRoles()).isNotNull().isEmpty();
            assertThat(user.getUserUuid()).isNull();
            assertThat(user.getUsername()).isEqualTo("u2");
        }

        @Test
        @DisplayName("findByEmail com doc roles null e userUuid null → User com roles vazias e userUuid null")
        void findByEmail_doc_com_nulls_retorna_user_corrigido() {
            UserDocument doc = new UserDocument();
            doc.setId("id3");
            doc.setUserUuid(null);
            doc.setUsername("u3");
            doc.setEmail("u3@example.com");
            doc.setPasswordHash("hash");
            doc.setEnabled(true);
            doc.setCreatedAt(LocalDateTime.now());
            doc.setRoles(null);

            when(repository.findByEmail("u3@example.com")).thenReturn(Optional.of(doc));

            Optional<User> result = adapter.findByEmail("u3@example.com");

            assertThat(result).isPresent();
            User user = result.get();
            assertThat(user.getRoles()).isNotNull().isEmpty();
            assertThat(user.getUserUuid()).isNull();
        }
    }
}
