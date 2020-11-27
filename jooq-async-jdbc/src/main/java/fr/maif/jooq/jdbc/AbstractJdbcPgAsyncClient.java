package fr.maif.jooq.jdbc;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.QueryResult;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.jooq.*;
import org.jooq.Record;

import java.util.concurrent.Executor;
import java.util.function.Function;

public abstract class AbstractJdbcPgAsyncClient implements PgAsyncClient {

    protected final SQLDialect dialect;
    protected final DSLContext client;
    protected final Executor executor;

    public AbstractJdbcPgAsyncClient(SQLDialect dialect, DSLContext client, Executor executor) {
        this.dialect = dialect;
        this.client = client;
        this.executor = executor;
    }

    @Override
    public <R extends Record> Future<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Future.of(executor, () ->
                queryFunction.apply(client).fetchOptional()
        ).map(r -> Option.ofOptional(r).map(JooqQueryResult::new));
    }

    @Override
    public <R extends Record> Future<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return Future.of(executor, () -> queryFunction.apply(client).fetch())
                .map(r ->
                    List.ofAll(r).map(JooqQueryResult::new)
                );

    }

    @Override
    public Future<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        return Future.of(executor, () -> queryFunction.apply(client).execute());
    }

    @Override
    public Future<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction) {
        return Future.of(executor, () ->
                client.batch(queryFunction.apply(client).toJavaList()).execute()
        ).map(b -> List.ofAll(b).sum().longValue());
    }

    @Override
    public Future<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        return Future.of(executor, () -> {
            if (values.isEmpty()) {
                return new int[]{};
            } else {
                return values.foldLeft(client.batch(queryFunction.apply(client)), (q, v) -> q.bind(v.toJavaArray(Object[]::new))).execute();
            }
        }).map(b -> List.ofAll(b).sum().longValue());
    }
}
