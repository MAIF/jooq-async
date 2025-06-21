package fr.maif.jooq.r2bdc;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.PgAsyncTransaction;
import io.r2dbc.spi.ConnectionFactory;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;


public class ReactivePgAsyncPool extends AbstractReactivePgAsyncClient implements PgAsyncPool {

    ConnectionFactory connectionFactory;
    private final Configuration configuration;

    public ReactivePgAsyncPool(ConnectionFactory client, Configuration configuration) {
        super(configuration == null ? DSL.using(client) : DSL.using(client, configuration.dialect()));
        connectionFactory = client;
        this.configuration = configuration;
    }

    @Override
    public CompletionStage<PgAsyncConnection> connection() {
        return Mono.from(connectionFactory.create())
                .<PgAsyncConnection>map(c -> new ReactivePgAsyncConnection(c, configuration))
                .toFuture();
    }

    @Override
    public CompletionStage<PgAsyncTransaction> begin() {
        return connection().thenCompose(c -> c.begin());
    }
}
