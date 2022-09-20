package fr.maif.jooq.reactor;

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

public interface PgAsyncClient {

    <R extends Record> Mono<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <R extends Record> Mono<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <Q extends Record> Flux<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction);
    Mono<Integer> execute(Function<DSLContext, ? extends Query> queryFunction);
    Mono<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction);
    Mono<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values);

}
