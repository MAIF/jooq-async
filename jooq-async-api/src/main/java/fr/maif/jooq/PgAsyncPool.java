package fr.maif.jooq;

import io.vavr.concurrent.Future;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface PgAsyncPool extends PgAsyncClient {
    Logger LOGGER = LoggerFactory.getLogger(PgAsyncPool.class);

    Future<PgAsyncConnection> connection();

    Future<PgAsyncTransaction> begin();

    default <T> Future<T> inTransaction(Function<PgAsyncTransaction, Future<T>> action) {
        return begin().flatMap(t ->
                action.apply(t)
                        .flatMap(r -> t.commit().map(__ -> r))
                        .recoverWith(e ->
                                t.rollback().flatMap(__ -> Future.failed(e))
                        )
        );
    }

    @Override
    default <Q extends Record> Flux<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Mono.fromCompletionStage(connection().toCompletableFuture())
                .flux()
                .concatMap(c -> Mono.fromCompletionStage(c.begin().toCompletableFuture()).flux()
                        .concatMap(pgAsyncTransaction -> pgAsyncTransaction
                                .stream(fetchSize, queryFunction)
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
