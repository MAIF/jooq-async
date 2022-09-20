package fr.maif.jooq.jdbc;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.QueryResult;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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
    public <R extends Record> CompletionStage<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return CompletableFuture
                .supplyAsync(() -> queryFunction.apply(client).fetchOptional(), executor)
                .thenApply(r -> Option.ofOptional(r).map(JooqQueryResult::new));
    }

    @Override
    public <R extends Record> CompletionStage<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return CompletableFuture
                .supplyAsync(() -> queryFunction.apply(client).fetch(), executor)
                .thenApply(r ->
                        List.ofAll(r).map(JooqQueryResult::new)
                );

    }

    @Override
    public CompletionStage<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        return CompletableFuture
                .supplyAsync(() -> queryFunction.apply(client).execute(), executor);
    }

    @Override
    public CompletionStage<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction) {
        return CompletableFuture
                .supplyAsync(() -> client.batch(queryFunction.apply(client).toJavaList()).execute(), executor)
                .thenApply(b -> List.ofAll(b).sum().longValue());
    }

    @Override
    public CompletionStage<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        return CompletableFuture
                .supplyAsync(() -> {
                    if (values.isEmpty()) {
                        return new int[]{};
                    } else {
                        return values.foldLeft(client.batch(queryFunction.apply(client)), (q, v) -> q.bind(v.toJavaArray(Object[]::new))).execute();
                    }
                }, executor)
                .thenApply(b -> List.ofAll(b).sum().longValue());
    }
}
