package fr.maif.jooq.reactive;

import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;
import static java.util.function.Function.identity;

public class ReactivePgAsyncTransaction extends AbstractReactivePgAsyncClient<SqlConnection> implements PgAsyncTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactivePgAsyncTransaction.class);

    private Transaction transaction;

    public ReactivePgAsyncTransaction(SqlConnection client, Transaction transaction, Configuration configuration) {
        super(client, configuration);
        this.transaction = transaction;
    }

    @Override
    public CompletionStage<Tuple0> commit() {
        return fromVertx(transaction.commit().flatMap(__ -> client.close())).thenApply(__ -> Tuple.empty());
    }

    @Override
    public CompletionStage<Tuple0> rollback() {
        return fromVertx(transaction.rollback().flatMap(__ -> client.close())).thenApply(__ -> Tuple.empty());
    }

    @Override
    public <Q extends Record> Flux<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        Query query = createQuery(queryFunction);
        log(query);
        AtomicBoolean first = new AtomicBoolean(true);
        return Mono.fromCompletionStage(this.client.prepare(toPreparedQuery(query)).map(q -> q.cursor(getBindValues(query))).toCompletionStage())
                .flatMapMany(c ->
                    Flux.using(
                        () -> c,
                        cursor -> Mono.just(List.<QueryResult>empty())
                        .expand(results -> {
                            if (first.getAndSet(false) || cursor.hasMore()) {
                                return Mono.fromCompletionStage(cursor.read(500).map(rs ->
                                        List.ofAll(rs)
                                                .map(ReactiveRowQueryResult::new)
                                                .map(r -> (QueryResult)r)
                                ).toCompletionStage());
                            } else {
                                return Mono.empty();
                            }
                        }),
                        Cursor::close)
                    )
                .flatMapIterable(identity());
    }
}
