package fr.maif.jooq;

import fr.maif.jooq.reactive.ReactivePgAsyncPool;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.junit.After;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveAsyncPoolTest extends AbstractPgAsyncPoolTest {

    private static Vertx vertx = Vertx.vertx();
    private Pool pool;

    @Override
    public PgAsyncPool pgAsyncPool(PostgreSQLContainer<?> postgreSQLContainer) {
        Configuration jooqConfig = new DefaultConfiguration().set(SQLDialect.POSTGRES);
        String host = postgreSQLContainer.getHost();
        Integer port = postgreSQLContainer.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);
        String databaseName = postgreSQLContainer.getDatabaseName();
        String username = postgreSQLContainer.getUsername();
        String password = postgreSQLContainer.getPassword();

        System.out.println(String.format("Reactive pool %s %d %s %s %s", host, port, databaseName, username, password));

        PgConnectOptions options = new PgConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(databaseName)
                .setUser(username)
                .setPassword(password);
        PoolOptions poolOptions = new PoolOptions().setMaxSize(3);
        pool = PgBuilder.pool()
                .using(vertx)
                .connectingTo(options)
                .with(poolOptions)
                .build();
        var connection = pool.getConnection().toCompletionStage().toCompletableFuture().join();
        connection.close().toCompletionStage().toCompletableFuture().join();
        return new ReactivePgAsyncPool(pool, jooqConfig);
    }

    @After
    public void atTheEnd() {
        pool.close();
    }

}
