package fr.maif.jooq;

import fr.maif.jooq.reactor.R2dbcAsyncPool;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.junit.After;
import org.testcontainers.containers.PostgreSQLContainer;

public class ReactiveAsyncPoolTest extends AbstractPgAsyncPoolTest {
    PgAsyncPool pgAsyncPool;
    ConnectionPool pool;

    ConnectionFactory connectionFactory;
    @Override
    public PgAsyncPool pgAsyncPool(PostgreSQLContainer<?> postgreSQLContainer) {
        //Configuration jooqConfig = new DefaultConfiguration().set(SQLDialect.POSTGRES);
        String host = postgreSQLContainer.getHost();
        Integer port = postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
        String databaseName = postgreSQLContainer.getDatabaseName();
        String username = postgreSQLContainer.getUsername();
        String password = postgreSQLContainer.getPassword();

        System.out.println(String.format("Reactive pool %s %d %s %s %s", host, port, databaseName, username, password));

        ConnectionFactory connectionFactory = new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .database(databaseName)
                .username(username)
                .password(password)
                .build());
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder()
                .connectionFactory(connectionFactory)
                .build();
        this.pool = new ConnectionPool(configuration);
        this.pgAsyncPool = R2dbcAsyncPool.create(pool, null);
        return pgAsyncPool;
    }

    @After
    public void atTheEnd() {
        this.pool.close();
    }

}
