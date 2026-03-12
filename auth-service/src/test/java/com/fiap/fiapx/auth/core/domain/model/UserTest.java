package com.fiap.fiapx.auth.core.domain.model;

import com.fiap.fiapx.auth.core.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User")
class UserTest {

    private static final String ID = "id1";
    private static final UUID USER_UUID = UUID.randomUUID();
    private static final String USERNAME = "user1";
    private static final String EMAIL = "user1@example.com";
    private static final String PASSWORD_HASH = "hash";
    private static final boolean ENABLED = true;
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2025, 1, 15, 10, 0);
    private static final Set<UserRole> ROLES = Set.of(UserRole.USER);

    @Nested
    @DisplayName("construtor completo")
    class ConstrutorCompleto {

        @Test
        @DisplayName("preenche todos os campos")
        void preenche_todos_os_campos() {
            User user = new User(ID, USER_UUID, USERNAME, EMAIL, PASSWORD_HASH, ENABLED, CREATED_AT, ROLES);

            assertThat(user.getId()).isEqualTo(ID);
            assertThat(user.getUserUuid()).isEqualTo(USER_UUID);
            assertThat(user.getUsername()).isEqualTo(USERNAME);
            assertThat(user.getEmail()).isEqualTo(EMAIL);
            assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
            assertThat(user.isEnabled()).isEqualTo(ENABLED);
            assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(user.getRoles()).isEqualTo(ROLES);
        }

        @Test
        @DisplayName("aceita userUuid e roles null")
        void aceita_nulls() {
            User user = new User(ID, null, USERNAME, EMAIL, PASSWORD_HASH, ENABLED, CREATED_AT, null);

            assertThat(user.getUserUuid()).isNull();
            assertThat(user.getRoles()).isNull();
        }
    }

    @Nested
    @DisplayName("construtor vazio + getters/setters")
    class GettersSetters {

        @Test
        @DisplayName("setters atualizam valores e getters retornam")
        void getters_e_setters() {
            User user = new User();

            user.setId(ID);
            user.setUserUuid(USER_UUID);
            user.setUsername(USERNAME);
            user.setEmail(EMAIL);
            user.setPasswordHash(PASSWORD_HASH);
            user.setEnabled(ENABLED);
            user.setCreatedAt(CREATED_AT);
            user.setRoles(ROLES);

            assertThat(user.getId()).isEqualTo(ID);
            assertThat(user.getUserUuid()).isEqualTo(USER_UUID);
            assertThat(user.getUsername()).isEqualTo(USERNAME);
            assertThat(user.getEmail()).isEqualTo(EMAIL);
            assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
            assertThat(user.isEnabled()).isEqualTo(ENABLED);
            assertThat(user.getCreatedAt()).isEqualTo(CREATED_AT);
            assertThat(user.getRoles()).isEqualTo(ROLES);
        }

        @Test
        @DisplayName("setEnabled false")
        void setEnabled_false() {
            User user = new User();
            user.setEnabled(false);
            assertThat(user.isEnabled()).isFalse();
        }
    }
}
