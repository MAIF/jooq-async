package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.QueryResult;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncClient {
    protected final PgAsyncClient underlying;

    public ReactorPgAsyncClient(PgAsyncClient underlying) {
        this.underlying = underlying;
    }

    @Override
    public <R extends Record> Mono<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Mono.fromCompletionStage(() ->
            underlying.queryOne(queryFunction)
        );
    }

    @Override
    public <R extends Record> Mono<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Mono.fromCompletionStage(() ->
                underlying.query(queryFunction)
        );
    }

    @Override
    public <Q extends Record> Flux<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Flux.from(underlying.stream(fetchSize, queryFunction));
    }

    @Override
    public Mono<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        return Mono.fromCompletionStage(() ->
                underlying.execute(queryFunction)
        );
    }

    @Override
    public Mono<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction) {
        return Mono.fromCompletionStage(() ->
                underlying.executeBatch(queryFunction)
        );
    }

    @Override
    public Mono<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        return Mono.fromCompletionStage(() ->
                underlying.executeBatch(queryFunction, values)
        );
    }
}
