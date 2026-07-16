package com.ufcstudy.persistence;

import org.flywaydb.core.Flyway;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;

public abstract class PersistenceIntegrationSupport {

    protected static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine")
                    .withDatabaseName("ufc_study_test")
                    .withUsername("ufc_study_test")
                    .withPassword("ufc_study_test");

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
        PGSimpleDataSource dataSource = new PGSimpleDataSource();

        dataSource.setURL(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());

        return dataSource;
    }

    protected NamedParameterJdbcTemplate jdbc(
            DataSource dataSource
    ) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    protected TransactionTemplate transactions(
            DataSource dataSource
    ) {
        return new TransactionTemplate(
                new DataSourceTransactionManager(dataSource)
        );
    }
}