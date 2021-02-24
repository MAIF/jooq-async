package fr.maif.jooq.reactive;

import akka.Done;
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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static java.util.function.Function.identity;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;

public class ReactivePgAsyncTransaction extends AbstractReactivePgAsyncClient<SqlConnection> implements PgAsyncTransaction {
import static java.util.function.Function.identity;

public class ReactivePgAsyncTransaction extends AbstractReactivePgAsyncClient<Transaction> implements PgAsyncTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactivePgAsyncTransaction.class);

    private Transaction transaction;

    public ReactivePgAsyncTransaction(SqlConnection client, Transaction transaction, Configuration configuration) {
        super(client, configuration);
        this.transaction = transaction;
    }

    @Override
    public Future<Tuple0> commit() {
        return fromVertx(transaction.commit().flatMap(__ -> client.close())).map(__ -> Tuple.empty());
    }

    @Override
    public Future<Tuple0> rollback() {
        return fromVertx(transaction.rollback().flatMap(__ -> client.close())).map(__ -> Tuple.empty());
    }

    @Override
    public <Q extends Record> Source<QueryResult, CompletionStage<PgAsyncTransaction>> stream(Integer fetchSize, boolean closeTx, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        Query query = createQuery(queryFunction);
        log(query);
        AtomicBoolean first = new AtomicBoolean(true);
        return Source.unfoldResourceAsync(
                () -> this.client.prepare(toPreparedQuery(query)).map(q -> q.cursor(getBindValues(query))).toCompletionStage(),
                cursor -> {
                    if (first.getAndSet(false) || cursor.hasMore()) {
                        return cursor.read(fetchSize).map(rs ->
                            Optional.of(List.ofAll(rs)
                                    .map(ReactiveRowQueryResult::new)
                                    .map(r -> (QueryResult)r))
                        ).toCompletionStage();
                    } else {
                        return CompletableFuture.completedFuture(Optional.empty());
                    }
                },
                cursor -> cursor.close().map(Done.getInstance()).toCompletionStage()
        ).mapConcat(l -> l)
        .watchTermination((tx, d) -> {
            CompletionStage<PgAsyncTransaction> mat = d.handleAsync((__, e) -> {
                if (e != null) {
                    return this.rollback().map(any -> (PgAsyncTransaction) this).toCompletableFuture();
                } else {
                    if (closeTx) {
                        return this.commit().map(any -> (PgAsyncTransaction) this).toCompletableFuture();
                    } else {
                        return CompletableFuture.completedFuture((PgAsyncTransaction) this);
                    }
                }
            })
                    .thenCompose(identity());
            return mat;
        });
    }
}
