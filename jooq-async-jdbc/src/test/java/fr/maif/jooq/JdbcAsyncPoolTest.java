package fr.maif.jooq;

import fr.maif.jooq.jdbc.JdbcPgAsyncPool;
import org.jooq.SQLDialect;

import java.util.concurrent.Executors;

public class JdbcAsyncPoolTest extends AbstractPgAsyncPoolTest {
    @Override
    public PgAsyncPool pgAsyncPool() {
        return new JdbcPgAsyncPool(SQLDialect.POSTGRES, dataSource, Executors.newFixedThreadPool(5));
    }
}
