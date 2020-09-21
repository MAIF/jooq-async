package fr.maif.jooq.jdbc;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import io.vavr.concurrent.Future;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

public class JdbcPgAsyncPool extends AbstractJdbcPgAsyncClient implements PgAsyncPool {

    private final DataSource dataSource;

    public JdbcPgAsyncPool(SQLDialect dialect, DataSource dataSource, Executor executor) {
        super(dialect, DSL.using(dataSource, dialect), executor);
        this.dataSource = dataSource;
    }

    @Override
    public Future<PgAsyncConnection> connection() {
        return Future.of(executor, () ->
                new JdbcPgAsyncConnection(dialect, dataSource.getConnection(), executor)
        );
    }

    @Override
    public Future<PgAsyncTransaction> begin() {
        return connection().flatMap(PgAsyncConnection::begin);
    }

}
