package fr.maif.jooq.reactive;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import io.vavr.concurrent.Future;
import io.vertx.pgclient.PgPool;
import org.jooq.Configuration;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;

public class ReactivePgAsyncPool extends AbstractReactivePgAsyncClient<PgPool> implements PgAsyncPool {

    public ReactivePgAsyncPool(PgPool client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public Future<PgAsyncConnection> connection() {
        return fromVertx(client.getConnection()).map(c -> new ReactivePgAsyncConnection(c, configuration));
    }

    @Override
    public Future<PgAsyncTransaction> begin() {
        return fromVertx(client.getConnection())
                .flatMap(c -> fromVertx(c.begin())
                        .map(t -> new ReactivePgAsyncTransaction(c, t, configuration))
                );
    }


}
