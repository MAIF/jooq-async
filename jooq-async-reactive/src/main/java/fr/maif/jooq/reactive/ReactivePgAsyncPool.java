package fr.maif.jooq.reactive;

import akka.stream.javadsl.Source;
import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ReactivePgAsyncPool extends AbstractReactivePgAsyncClient<PgPool> implements PgAsyncPool {

    public ReactivePgAsyncPool(PgPool client, Configuration configuration) {
        super(client, configuration);
    }

    @Override
    public Future<PgAsyncConnection> connection() {
        Promise<SqlConnection> fConnection = Promise.make();
        client.getConnection(toCompletionHandler(fConnection));
        return fConnection.future().map(c -> new ReactivePgAsyncConnection(c, configuration));
    }

    @Override
    public Future<PgAsyncTransaction> begin() {
        Promise<Transaction> fConnection = Promise.make();
        client.begin(toCompletionHandler(fConnection));
        return fConnection.future().map(c -> new ReactivePgAsyncTransaction(c, configuration));
    }

}
