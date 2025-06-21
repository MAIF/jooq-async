package fr.maif.jooq.reactor;

import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.r2bdc.ReactivePgAsyncPool;
import io.r2dbc.spi.ConnectionFactory;
import org.jooq.Configuration;

public class R2dbcAsyncPool {

    public static PgAsyncPool create(ConnectionFactory client, Configuration configuration) {
        PgAsyncPool pool = new ReactivePgAsyncPool(client, configuration);
        return pool;
    }
}
