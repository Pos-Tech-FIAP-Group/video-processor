package com.fiap.fiapx.video.infra.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class FlywayPostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deve_aplicar_migrations_do_flyway_e_criar_tabelas() {
        Integer flywayTableExists = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = 'public' and table_name = 'flyway_schema_history'
                """, Integer.class);

        Integer videosTableExists = jdbcTemplate.queryForObject("""
                select count(*)
                from information_schema.tables
                where table_schema = 'public' and table_name = 'videos'
                """, Integer.class);

        assertThat(flywayTableExists).isEqualTo(1);
        assertThat(videosTableExists).isEqualTo(1);

        Integer appliedMigrations = jdbcTemplate.queryForObject("""
                select count(*) from flyway_schema_history where success = true
                """, Integer.class);

        assertThat(appliedMigrations).isGreaterThanOrEqualTo(1);
    }
}