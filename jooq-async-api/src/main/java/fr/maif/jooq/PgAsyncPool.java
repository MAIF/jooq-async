package fr.maif.jooq;

import io.vavr.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface PgAsyncPool extends PgAsyncClient {
    Logger LOGGER = LoggerFactory.getLogger(PgAsyncPool.class);

    CompletionStage<PgAsyncConnection> connection();

    CompletionStage<PgAsyncTransaction> begin();

    default <T> CompletionStage<T> inTransaction(Function<PgAsyncTransaction, CompletionStage<T>> action) {
        return begin().thenCompose(t ->
                action.apply(t)
                        .thenCompose(r -> t.commit().thenApply(__ -> r))
                        .exceptionallyCompose(e ->
                                t.rollback().thenCompose(__ -> CompletableFuture.failedStage(e))
                        )
        );
    }
}
