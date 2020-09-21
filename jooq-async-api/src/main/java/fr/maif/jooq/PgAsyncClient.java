package fr.maif.jooq;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;

import java.util.function.Function;

public interface PgAsyncClient {

    <R extends Record> Future<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <R extends Record> Future<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction);
    <Q extends Record> Source<QueryResult, NotUsed> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction);
    Future<Integer> execute(Function<DSLContext, ? extends Query> queryFunction);
    Future<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction);
    Future<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values);

}
