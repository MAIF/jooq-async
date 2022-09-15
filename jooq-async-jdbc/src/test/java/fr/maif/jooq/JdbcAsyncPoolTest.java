package fr.maif.jooq;

import fr.maif.jooq.jdbc.JdbcPgAsyncPool;
import org.jooq.SQLDialect;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.concurrent.Executors;

public class JdbcAsyncPoolTest extends AbstractPgAsyncPoolTest {

    @Override
    public PgAsyncPool pgAsyncPool(PostgreSQLContainer<?> postgreSQLContainer) {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setUrl(postgreSQLContainer.getJdbcUrl());
        pgSimpleDataSource.setUser(postgreSQLContainer.getUsername());
        pgSimpleDataSource.setPassword(postgreSQLContainer.getPassword());
        return new JdbcPgAsyncPool(SQLDialect.POSTGRES, pgSimpleDataSource, Executors.newFixedThreadPool(5));
    }
}
