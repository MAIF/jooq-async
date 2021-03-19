package fr.maif.jooq.reactive;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import io.vavr.concurrent.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.TransactionRollbackException;
import org.jooq.Configuration;

import java.util.function.Function;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;

public class ReactivePgAsyncPool extends AbstractReactivePgAsyncClient<PgPool> implements PgAsyncPool {

    public ReactivePgAsyncPool(PgPool client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public <T> Future<T> inTransaction(Function<PgAsyncTransaction, Future<T>> action) {
        return FutureConversions.fromVertx(client.getConnection()
                .flatMap(conn -> conn
                        .begin()
                        .flatMap(tx -> FutureConversions.toVertx(action
                                .apply(new ReactivePgAsyncTransaction(conn, tx, configuration)))
                                .compose(
                                        res -> tx
                                                .commit()
                                                .flatMap(v -> io.vertx.core.Future.succeededFuture(res)),
                                        err -> {
                                            if (err instanceof TransactionRollbackException) {
                                                return io.vertx.core.Future.failedFuture(err);
                                            } else {
                                                return tx
                                                        .rollback()
                                                        .compose(v -> io.vertx.core.Future.failedFuture(err), failure -> io.vertx.core.Future.failedFuture(err));
                                            }
                                        }))
                        .onComplete(ar -> conn.close()))
        );
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
