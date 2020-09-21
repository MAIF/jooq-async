package fr.maif.jooq;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.concurrent.Future;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    default <Q extends Record> Source<QueryResult, NotUsed> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Source.fromSourceCompletionStage(begin().map(pgAsyncTransaction -> pgAsyncTransaction
                        .stream(fetchSize, queryFunction)
                        .watchTermination((nu, d) ->
                                d.handleAsync((__, e) -> {
                                    if (e != null) {
                                        LOGGER.error("Stream terminated with error", e);
                                        return pgAsyncTransaction.rollback().toCompletableFuture();
                                    } else {
                                        LOGGER.debug("Stream terminated correctly");
                                        return pgAsyncTransaction.commit().toCompletableFuture();
                                    }
                                })
                        )
                ).toCompletableFuture()
        ).mapMaterializedValue(__ -> NotUsed.notUsed());
    }
}
