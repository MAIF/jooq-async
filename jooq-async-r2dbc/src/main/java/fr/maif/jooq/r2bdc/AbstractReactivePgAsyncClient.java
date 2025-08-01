package fr.maif.jooq.r2bdc;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.QueryResult;
import io.r2dbc.postgresql.codec.Json;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public abstract class AbstractReactivePgAsyncClient implements PgAsyncClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractReactivePgAsyncClient.class);

    protected final DSLContext dslContext;

    public AbstractReactivePgAsyncClient(DSLContext context) {

        this.dslContext = context;
    }

    public <R extends Record> CompletionStage<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Flux.from(queryFunction.apply(dslContext))
                .collectList()
                .map(r -> List.ofAll(r).headOption().<QueryResult>map(JooqQueryResult::new))
                .toFuture();
    }

    public <R extends Record> CompletionStage<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Flux.from(queryFunction.apply(dslContext))
                .collectList()
                .map(r -> List.ofAll(r).<QueryResult>map(JooqQueryResult::new))
                .toFuture();
    }

    public <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Flux.from(queryFunction.apply(dslContext)).map(JooqQueryResult::new);
    }


    public CompletionStage<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        Query query = queryFunction.apply(dslContext);
        if (query instanceof RowCountQuery countQuery) {
            return Mono.from(countQuery).toFuture();
        } else {
            return Mono.<Integer>error(new RuntimeException("Invalid query"+query)).toFuture();
        }
    }

    public CompletionStage<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction) {
        List<? extends Query> queries = queryFunction.apply(dslContext);
        return Flux.from(dslContext.batch(queries.toJavaList())).collectList()
                .map(l -> List.ofAll(l).foldLeft(0L, (acc, elt) -> acc +elt))
                .toFuture();
    }

    public CompletionStage<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        Query query = queryFunction.apply(dslContext);
        if (query instanceof InsertValuesStepN<?> insertValuesStepN) {
            return Flux.from(insertValuesStepN.values(values.map(List::asJava).asJava()))
                    .collectList()
                    .map(list -> List.ofAll(list).foldLeft(0L, (acc, elt) -> acc +elt))
                    .toFuture();
        } else {
            return Mono.<Long>error(new RuntimeException("Query type not supported "+query)).toFuture();
        }
    }

}
