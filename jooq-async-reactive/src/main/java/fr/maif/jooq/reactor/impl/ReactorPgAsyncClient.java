package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.QueryResult;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncClient, fr.maif.jooq.PgAsyncClient {
    protected final PgAsyncClient underlying;

    public ReactorPgAsyncClient(PgAsyncClient underlying) {
        this.underlying = underlying;
    }

    @Override
    public <R extends Record> Mono<Option<QueryResult>> queryOneMono(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Mono.fromCompletionStage(() ->
            underlying.queryOne(queryFunction)
        );
    }

    @Override
    public <R extends Record> Mono<List<QueryResult>> queryMono(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Mono.fromCompletionStage(() ->
                underlying.query(queryFunction)
        );
    }

    @Override
    public <Q extends Record> Flux<QueryResult> streamFlux(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Flux.from(underlying.stream(fetchSize, queryFunction));
    }

    @Override
    public Mono<Integer> executeMono(Function<DSLContext, ? extends Query> queryFunction) {
        return Mono.fromCompletionStage(() ->
                underlying.execute(queryFunction)
        );
    }

    @Override
    public Mono<Long> executeBatchMono(Function<DSLContext, List<? extends Query>> queryFunction) {
        return Mono.fromCompletionStage(() ->
                underlying.executeBatch(queryFunction)
        );
    }

    @Override
    public Mono<Long> executeBatchMono(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        return Mono.fromCompletionStage(() ->
                underlying.executeBatch(queryFunction, values)
        );
    }

    @Override
    public <R extends Record> CompletionStage<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return underlying.queryOne(queryFunction);
    }

    @Override
    public <R extends Record> CompletionStage<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return underlying.query(queryFunction);
    }

    @Override
    public <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return underlying.stream(fetchSize, queryFunction);
    }

    @Override
    public CompletionStage<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        return underlying.execute(queryFunction);
    }

    @Override
    public CompletionStage<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction) {
        return underlying.executeBatch(queryFunction);
    }

    @Override
    public CompletionStage<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        return underlying.executeBatch(queryFunction, values);
    }
}
