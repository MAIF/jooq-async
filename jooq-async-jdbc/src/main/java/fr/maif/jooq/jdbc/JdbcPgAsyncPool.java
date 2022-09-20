package fr.maif.jooq.jdbc;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import fr.maif.jooq.QueryResult;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class JdbcPgAsyncPool extends AbstractJdbcPgAsyncClient implements PgAsyncPool {

    private final DataSource dataSource;

    public JdbcPgAsyncPool(SQLDialect dialect, DataSource dataSource, Executor executor) {
        super(dialect, DSL.using(dataSource, dialect), executor);
        this.dataSource = dataSource;
    }

    @Override
    public CompletionStage<PgAsyncConnection> connection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new JdbcPgAsyncConnection(dialect, dataSource.getConnection(), executor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @Override
    public CompletionStage<PgAsyncTransaction> begin() {
        return connection().thenCompose(PgAsyncConnection::begin);
    }

    @Override
    public <Q extends Record> Publisher<QueryResult> stream(Integer fetchSize, Function<DSLContext, ? extends ResultQuery<Q>> queryFunction) {
        return Mono.fromCompletionStage(connection())
                .flux()
                .concatMap(c -> Mono.fromCompletionStage(c.begin())
                        .flux()
                        .concatMap(pgAsyncTransaction -> Flux.from(pgAsyncTransaction.stream(fetchSize, queryFunction))
                                .doOnComplete(() -> {
                                    LOGGER.debug("Stream terminated correctly");
                                    pgAsyncTransaction.commit();
                                })
                                .doOnError(e -> {
                                    LOGGER.error("Stream terminated with error", e);
                                    pgAsyncTransaction.rollback();
                                })
                        )
                );
    }
}
