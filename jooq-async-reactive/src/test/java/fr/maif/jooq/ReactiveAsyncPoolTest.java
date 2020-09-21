package fr.maif.jooq;

import fr.maif.jooq.reactive.ReactivePgAsyncPool;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.concurrent.atomic.AtomicReference;

public class ReactiveAsyncPoolTest extends AbstractPgAsyncPoolTest {

    private static Vertx vertx = Vertx.vertx();
    private static AtomicReference<ReactivePgAsyncPool> thePoolRef = new AtomicReference<>();
    private static AtomicReference<PgPool> clientRef = new AtomicReference<>();

    @Override
    public PgAsyncPool pgAsyncPool() {
        return thePoolRef.get();
    }

    @AfterClass
    public static void atTheEnd() {
        if (clientRef.get() != null) {
            clientRef.get().close();
            clientRef.set(null);
        }
    }

    @BeforeClass
    public static void createPool() {
        DefaultConfiguration jooqConfig = new DefaultConfiguration();
        jooqConfig.setSQLDialect(SQLDialect.POSTGRES);
        PgConnectOptions options = new PgConnectOptions()
                .setPort(port)
                .setHost("localhost")
                .setDatabase(database)
                .setUser(user)
                .setPassword(password);
        PoolOptions poolOptions = new PoolOptions().setMaxSize(3);
        clientRef.set(PgPool.pool(vertx, options, poolOptions));

        thePoolRef.set(new ReactivePgAsyncPool(clientRef.get(), jooqConfig));
    }

}
