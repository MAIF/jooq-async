package fr.maif.jooq;

import akka.Done;
import akka.japi.function.Function2;
import akka.stream.javadsl.Source;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface PgAsyncClient {

    <R extends Record> Future<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);

    <R extends Record> Future<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);

    <Q extends Record> Source<QueryResult, CompletionStage<PgAsyncTransaction>> stream(Integer fetchSize, boolean commit, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction);

    default <Q extends Record> Source<QueryResult, CompletionStage<PgAsyncTransaction>> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return stream(fetchSize, true, queryFunction);
    }

    Future<Integer> execute(Function<DSLContext, ? extends Query> queryFunction);

    Future<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction);

    Future<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values);


    static Function2<CompletionStage<PgAsyncTransaction>, CompletionStage<Done>, CompletionStage<PgAsyncTransaction>> commitIfSuccess() {
        return (tx, d) ->
                d.handle((__, e) -> {
                    if (e != null) {
                        return tx.thenCompose(transact ->
                                transact
                                        .rollback()
                                        .map(___ -> transact)
                                        .toCompletableFuture()
                        );
                    } else {
                        return tx.thenCompose(transact ->
                                transact
                                        .commit()
                                        .map(___ -> transact)
                                        .toCompletableFuture()
                        );
                    }
                }).thenCompose(Function.identity());
    }

    static <T> Function2<T, CompletionStage<Done>, T> commitIfSuccess(PgAsyncTransaction tx) {
        return commitIfSuccess(true, tx);
    }

    static <T> Function2<T, CompletionStage<Done>, T> commitIfSuccess(boolean shouldCommit, PgAsyncTransaction tx) {
        return (any, d) -> {
            d.handle((__, e) -> {
                if (e != null) {
                    return tx.rollback()
                            .flatMap(dumy -> Future.<T>failed(e))
                            .toCompletableFuture();
                } else {
                    if (shouldCommit) {
                        return tx.commit()
                                .map(___ -> any)
                                .toCompletableFuture();
                    } else {
                        return CompletableFuture.completedFuture(any);
                    }
                }
            }).thenCompose(Function.identity());
            return any;
        };
    }
}
