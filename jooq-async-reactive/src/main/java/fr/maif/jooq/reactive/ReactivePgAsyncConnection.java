package fr.maif.jooq.reactive;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.concurrent.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;

public class ReactivePgAsyncConnection extends AbstractReactivePgAsyncClient<SqlConnection> implements PgAsyncConnection {

    public ReactivePgAsyncConnection(SqlConnection client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public CompletionStage<Tuple0> close() {
        return fromVertx(client.close()).thenApply(__ -> Tuple.empty());
    }

    @Override
    public CompletionStage<PgAsyncTransaction> begin() {
        return fromVertx(client.begin())
                .thenApply(tx -> new ReactivePgAsyncTransaction(client, tx, configuration));
    }

    @Override
    public <Q extends Record> Flux<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Mono.fromCompletionStage(client.begin().toCompletionStage())
                .flatMapMany(tx -> {
                            final ReactivePgAsyncTransaction pgAsyncTransaction = new ReactivePgAsyncTransaction(client, tx, configuration);
                            return pgAsyncTransaction
                                    .stream(fetchSize, queryFunction)
                                    .doOnComplete(() -> {
                                        pgAsyncTransaction.commit();
                                    })
                                    .doOnError(e -> {
                                        pgAsyncTransaction.rollback();
                                    });
                        });
    }

}
