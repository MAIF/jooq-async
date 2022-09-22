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

    <R extends Record> Mono<Option<QueryResult>> queryOneMono(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <R extends Record> Mono<List<QueryResult>> queryMono(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <Q extends Record> Flux<QueryResult> streamFlux(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction);
    Mono<Integer> executeMono(Function<DSLContext, ? extends Query> queryFunction);
    Mono<Long> executeBatchMono(Function<DSLContext, List<? extends Query>> queryFunction);
    Mono<Long> executeBatchMono(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values);

}
