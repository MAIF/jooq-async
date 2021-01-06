package fr.maif.jooq.reactive;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jooq.Record;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;

public class ReactivePgAsyncTransaction extends AbstractReactivePgAsyncClient<SqlConnection> implements PgAsyncTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactivePgAsyncTransaction.class);

    private Transaction transaction;

    public ReactivePgAsyncTransaction(SqlConnection client, Transaction transaction, Configuration configuration) {
        super(client, configuration);
        this.transaction = transaction;
    }

    @Override
    public Future<Tuple0> commit() {
        return fromVertx(transaction.commit()).map(__ -> Tuple.empty());
    }

    @Override
    public Future<Tuple0> rollback() {
        return fromVertx(transaction.rollback()).map(__ -> Tuple.empty());
    }

    @Override
    public <Q extends Record> Source<QueryResult, NotUsed> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        Query query = createQuery(queryFunction);
        log(query);
        AtomicBoolean first = new AtomicBoolean(true);
        return Source.unfoldResourceAsync(
                () -> this.client.prepare(toPreparedQuery(query)).map(q -> q.cursor(getBindValues(query))).toCompletionStage(),
                cursor -> {
                    if (first.getAndSet(false) || cursor.hasMore()) {
                        return cursor.read(500).map(rs ->
                            Optional.of(List.ofAll(rs)
                                    .map(ReactiveRowQueryResult::new)
                                    .map(r -> (QueryResult)r))
                        ).toCompletionStage();
                    } else {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                },
                cursor -> cursor.close().map(Done.getInstance()).toCompletionStage()
        ).mapConcat(l -> l);
    }
}
