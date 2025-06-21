package fr.maif.jooq.reactor;

import fr.maif.jooq.reactor.impl.ReactorPgAsyncPool;
import io.vertx.sqlclient.Pool;
import org.jooq.Configuration;
import fr.maif.jooq.vertx.ReactivePgAsyncPool;

public class VertxPgAsyncPool {

    public static PgAsyncPool create(Pool client, Configuration configuration) {
        ReactivePgAsyncPool pool = new ReactivePgAsyncPool(client, configuration);
        return new ReactorPgAsyncPool(pool);
    }

    public static PgAsyncPool create(ReactivePgAsyncPool pgAsyncPool) {
        return new ReactorPgAsyncPool(pgAsyncPool);
    }

}
