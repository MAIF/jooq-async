package fr.maif.jooq.jdbc;

import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JdbcPgAsyncTransaction extends AbstractJdbcPgAsyncClient implements PgAsyncTransaction {

    private final Connection connection;

    public JdbcPgAsyncTransaction(SQLDialect dialect, Connection connection, Executor executor) {
        super(dialect, DSL.using(connection, dialect), executor);
        this.connection = connection;
    }

    @Override
    public CompletionStage<Void> commit() {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, executor);
    }

    @Override
    public CompletionStage<Void> rollback() {
        return CompletableFuture.runAsync(() -> {
            try {
                connection.rollback();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    connection.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, executor);
    }

    @Override
    public <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Flux
                .fromIterable(() -> queryFunction.apply(client).stream().iterator())
                .publishOn(Schedulers.parallel())
                .map(JooqQueryResult::new);
    }
}
