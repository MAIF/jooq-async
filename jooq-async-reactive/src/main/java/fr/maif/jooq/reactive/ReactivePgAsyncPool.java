package fr.maif.jooq.reactive;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jooq.Configuration;

public class ReactivePgAsyncPool extends AbstractReactivePgAsyncClient<PgPool> implements PgAsyncPool {

    public ReactivePgAsyncPool(PgPool client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public Future<PgAsyncConnection> connection() {
        Promise<SqlConnection> fConnection = Promise.make();
        client.getConnection(toCompletionHandler(fConnection));
        return fConnection.future().map(c -> new ReactivePgAsyncConnection(c, configuration));
    }

    @Override
    public Future<PgAsyncTransaction> begin() {
        return FutureConversions.fromVertx(client.getConnection())
                .flatMap(c -> FutureConversions.fromVertx(c.begin())
                        .map(t -> new ReactivePgAsyncTransaction(c, t, configuration))
                );
    }


}
