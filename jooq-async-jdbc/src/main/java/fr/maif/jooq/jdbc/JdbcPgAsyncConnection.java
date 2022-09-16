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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JdbcPgAsyncConnection extends AbstractJdbcPgAsyncClient implements PgAsyncConnection {

    private final Connection connection;

    public JdbcPgAsyncConnection(SQLDialect dialect, Connection connection, Executor executor) {
        super(dialect, DSL.using(connection, dialect), executor);
        this.connection = connection;
    }

    @Override
    public Future<Tuple0> close() {
        return Future.of(executor, () -> {
            connection.close();
            return Tuple.empty();
        });
    }

    @Override
    public Future<PgAsyncTransaction> begin() {
        return Future.of(executor, () -> {
            connection.setAutoCommit(false);
            return new JdbcPgAsyncTransaction(dialect, connection, executor);
        });
    }

    @Override
    public <Q extends Record> Flux<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Mono.fromCompletionStage(begin().toCompletableFuture())
                .flux()
                .concatMap(pgAsyncTransaction ->
                        pgAsyncTransaction.stream(fetchSize, queryFunction)
                );
    }

}
