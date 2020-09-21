package fr.maif.jooq.reactive;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import io.vavr.control.Try;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Transaction;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class ReactivePgAsyncTransaction extends AbstractReactivePgAsyncClient<Transaction> implements PgAsyncTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactivePgAsyncTransaction.class);

    public ReactivePgAsyncTransaction(Transaction client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public Future<Tuple0> commit() {
        Promise<Tuple0> fCommit = Promise.make();
        client.commit(r -> fCommit.complete(Try.of(Tuple::empty)));
        return fCommit.future();
    }

    @Override
    public Future<Tuple0> rollback() {
        Promise<Tuple0> fRollback = Promise.make();
        client.rollback(r -> fRollback.complete(Try.of(Tuple::empty)));
        return fRollback.future();
    }

    @Override
    public <Q extends Record> Source<QueryResult, NotUsed> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        Query query = createQuery(queryFunction);
        log(query);

        Promise<PreparedQuery> fPreparedQuery = Promise.make();
        Handler<AsyncResult<PreparedQuery>> preparedQueryFutureHandler = toCompletionHandler(fPreparedQuery);
        this.client.prepare(toPreparedQuery(query), preparedQueryFutureHandler);
        AtomicBoolean first = new AtomicBoolean(true);
        return Source.unfoldResourceAsync(
                () -> fPreparedQuery.future().map(q -> q.cursor(getBindValues(query)))
                            .toCompletableFuture(),
                cursor -> {
                    if (first.getAndSet(false) || cursor.hasMore()) {
                        Promise<RowSet<Row>> resultP = Promise.make();
                        Handler<AsyncResult<RowSet<Row>>> resultHandler = toCompletionHandler(resultP);
                        cursor.read(500, resultHandler);
                        return resultP.future().map(rs ->
                            Optional.of(List.ofAll(rs)
                                    .map(ReactiveRowQueryResult::new)
                                    .map(r -> (QueryResult)r))
                        ).toCompletableFuture();
                    } else {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                },
                cursor -> CompletableFuture.completedFuture(Done.getInstance()))
                .mapConcat(l -> l);
    }
}
