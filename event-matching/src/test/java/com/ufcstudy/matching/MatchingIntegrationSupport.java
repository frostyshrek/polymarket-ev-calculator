package com.ufcstudy.matching;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

public abstract class MatchingIntegrationSupport {

    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("matching_test")
                    .withUsername("matching_test")
                    .withPassword("matching_test");

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
        PGSimpleDataSource source = new PGSimpleDataSource();

        source.setURL(POSTGRES.getJdbcUrl());
        source.setUser(POSTGRES.getUsername());
        source.setPassword(POSTGRES.getPassword());

        return source;
    }

    protected NamedParameterJdbcTemplate jdbc(
            DataSource source
    ) {
        return new NamedParameterJdbcTemplate(source);
    }

    protected TransactionTemplate transactions(
            DataSource source
    ) {
        return new TransactionTemplate(
                new DataSourceTransactionManager(source)
        );
    }
}