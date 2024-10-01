package fr.maif.jooq.reactive;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.concurrent.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.TransactionRollbackException;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;

public class ReactivePgAsyncPool extends AbstractReactivePgAsyncClient<Pool> implements PgAsyncPool {

    public ReactivePgAsyncPool(Pool client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public <T> CompletionStage<T> inTransaction(Function<PgAsyncTransaction, CompletionStage<T>> action) {
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
    public CompletionStage<PgAsyncConnection> connection() {
        return fromVertx(client.getConnection()).thenApply(c -> new ReactivePgAsyncConnection(c, configuration));
    }

    @Override
    public CompletionStage<PgAsyncTransaction> begin() {
        return fromVertx(client.getConnection())
                .thenCompose(c -> fromVertx(c.begin())
                        .thenApply(t -> new ReactivePgAsyncTransaction(c, t, configuration))
                );
    }

    @Override
    public <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Mono.fromCompletionStage(connection())
                .flux()
                .concatMap(c -> Mono.fromCompletionStage(c.begin())
                        .flux()
                        .concatMap(pgAsyncTransaction -> Flux.from(pgAsyncTransaction.stream(fetchSize, queryFunction))
                                .doOnComplete(() -> {
                                    LOGGER.debug("Stream terminated correctly");
                                    pgAsyncTransaction.commit();
                                })
                                .doOnError(e -> {
                                    LOGGER.error("Stream terminated with error", e);
                                    pgAsyncTransaction.rollback();
                                })
                        )
                );
    }


}
