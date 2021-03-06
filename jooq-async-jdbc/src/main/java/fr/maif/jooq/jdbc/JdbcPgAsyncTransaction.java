package fr.maif.jooq.jdbc;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import io.vavr.Tuple0;
import io.vavr.Tuple;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JdbcPgAsyncTransaction extends AbstractJdbcPgAsyncClient implements PgAsyncTransaction {

    private final Connection connection;

    public JdbcPgAsyncTransaction(SQLDialect dialect, Connection connection, Executor executor) {
        super(dialect, DSL.using(connection, dialect), executor);
        this.connection = connection;
    }

    @Override
    public Future<Tuple0> commit() {
        return Future.of(executor, () -> {
            Try.of(() -> {
                connection.commit();
                return Tuple.empty();
            });
            connection.close();
            return Tuple.empty();
        });
    }

    @Override
    public Future<Tuple0> rollback() {
        return Future.of(executor, () -> {
            Try.of(() -> {
                connection.rollback();
                return Tuple.empty();
            });
            connection.close();
            return Tuple.empty();
        });
    }

    @Override
    public <Q extends Record> Source<QueryResult, NotUsed> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Source
                .fromIterator(() -> queryFunction.apply(client).stream().iterator())
                .async("jdbc-execution-context")
                .map(JooqQueryResult::new);
    }
}
