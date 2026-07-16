package com.ufcstudy.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class DatabaseMigrationTest {

    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("ufc_study_test")
                    .withUsername("ufc_study_test")
                    .withPassword("ufc_study_test");

    @BeforeAll
    static void migrateDatabase() {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations("classpath:db/migration")
                .validateMigrationNaming(true)
                .load();

        var result = flyway.migrate();

        assertEquals(9, result.migrationsExecuted);
    }

    @Test
    void createsStudySchema() throws SQLException {
        try (
                Connection connection = POSTGRES.createConnection("");
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.schemata
                            WHERE schema_name = 'ufc_study'
                        )
                        """
                );
                ResultSet resultSet = statement.executeQuery()
        ) {
            assertTrue(resultSet.next());
            assertTrue(resultSet.getBoolean(1));
        }
    }

    @Test
    void createsExpectedTables() throws SQLException {
        try (
                Connection connection = POSTGRES.createConnection("");
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'ufc_study'
                        """
                );
                ResultSet resultSet = statement.executeQuery()
        ) {
            assertTrue(resultSet.next());

            int tableCount = resultSet.getInt(1);

            assertEquals(23, tableCount);
        }
    }

    @Test
    void createsFlywayHistory() throws SQLException {
        try (
                Connection connection = POSTGRES.createConnection("");
                PreparedStatement statement = connection.prepareStatement(
                        """
                        SELECT COUNT(*)
                        FROM public.flyway_schema_history
                        WHERE success = TRUE
                        """
                );
                ResultSet resultSet = statement.executeQuery()
        ) {
            assertTrue(resultSet.next());
            assertEquals(9, resultSet.getInt(1));
        }
    }
}