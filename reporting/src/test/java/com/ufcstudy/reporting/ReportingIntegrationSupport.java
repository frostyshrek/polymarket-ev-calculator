package com.ufcstudy.reporting;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

public abstract class ReportingIntegrationSupport {

    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("reporting_test")
                    .withUsername("reporting_test")
                    .withPassword("reporting_test");

    static {
        POSTGRES.start();

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }

    protected DataSource dataSource() {
        PGSimpleDataSource source =
                new PGSimpleDataSource();

        source.setURL(POSTGRES.getJdbcUrl());
        source.setUser(POSTGRES.getUsername());
        source.setPassword(POSTGRES.getPassword());

        return source;
    }

    protected NamedParameterJdbcTemplate jdbc(
            DataSource dataSource
    ) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}