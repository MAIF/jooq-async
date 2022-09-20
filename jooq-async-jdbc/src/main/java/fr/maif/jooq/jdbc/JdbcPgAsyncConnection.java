package fr.maif.jooq.jdbc;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.concurrent.Future;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JdbcPgAsyncConnection extends AbstractJdbcPgAsyncClient implements PgAsyncConnection {

    private final Connection connection;

    public JdbcPgAsyncConnection(SQLDialect dialect, Connection connection, Executor executor) {
        super(dialect, DSL.using(connection, dialect), executor);
        this.connection = connection;
    }

    @Override
    public CompletionStage<Void> close() {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletionStage<PgAsyncTransaction> begin() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return new JdbcPgAsyncTransaction(dialect, connection, executor);
        }, executor);
    }

    @Override
    public <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Mono.fromCompletionStage(begin().toCompletableFuture())
                .flux()
                .concatMap(pgAsyncTransaction ->
                        pgAsyncTransaction.stream(fetchSize, queryFunction)
                );
    }

}
