package fr.maif.jooq;

import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface PgAsyncClient {

    <R extends Record> CompletionStage<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <R extends Record> CompletionStage<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction);
    CompletionStage<Integer> execute(Function<DSLContext, ? extends Query> queryFunction);
    CompletionStage<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction);
    CompletionStage<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values);

}
