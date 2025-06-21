package fr.maif.jooq.r2bdc;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncTransaction;
import io.r2dbc.spi.Connection;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;

public class ReactivePgAsyncConnection extends AbstractReactivePgAsyncClient implements PgAsyncConnection {

    Connection connection;
    private final Configuration configuration;

    public ReactivePgAsyncConnection(Connection connection, Configuration configuration) {
        super(configuration == null ? DSL.using(connection) : DSL.using(connection, configuration.dialect()));
        this.connection = connection;
        this.configuration = configuration;
    }

    @Override
    public CompletionStage<Tuple0> close() {
        return Mono.from(connection.close()).map(any -> Tuple.empty()).toFuture();
    }

    @Override
    public CompletionStage<PgAsyncTransaction> begin() {
        return Mono.from(connection.beginTransaction())
                .<PgAsyncTransaction>then(Mono.just(new ReactivePgAsyncTransaction(connection, configuration)))
                .toFuture();
    }

}
