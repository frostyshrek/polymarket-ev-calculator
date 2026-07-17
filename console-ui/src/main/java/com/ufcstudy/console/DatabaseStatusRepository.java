package com.ufcstudy.console;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class DatabaseStatusRepository {

    private final JdbcTemplate jdbc;

    public DatabaseStatusRepository(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public DatabaseStatus readStatus() {
        String databaseVersion =
                jdbc.queryForObject(
                        "SELECT version()",
                        String.class
                );

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM public.flyway_schema_history
                        WHERE success = TRUE
                        """,
                        Integer.class
                );

        String currentVersion =
                jdbc.queryForObject(
                        """
                        SELECT COALESCE(MAX(version), 'none')
                        FROM public.flyway_schema_history
                        WHERE success = TRUE
                        """,
                        String.class
                );

        Integer tableCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'ufc_study'
                          AND table_type = 'BASE TABLE'
                        """,
                        Integer.class
                );

        return new DatabaseStatus(
                databaseVersion,
                migrationCount == null ? 0 : migrationCount,
                currentVersion,
                tableCount == null ? 0 : tableCount
        );
    }

    public record DatabaseStatus(
            String databaseVersion,
            int successfulMigrations,
            String currentMigrationVersion,
            int applicationTables
    ) {
    }
}