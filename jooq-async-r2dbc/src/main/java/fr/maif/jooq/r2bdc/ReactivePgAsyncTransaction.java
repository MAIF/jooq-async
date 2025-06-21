package fr.maif.jooq.r2bdc;

import fr.maif.jooq.PgAsyncTransaction;
import io.r2dbc.spi.Connection;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import org.jooq.Configuration;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;

public class ReactivePgAsyncTransaction extends AbstractReactivePgAsyncClient implements PgAsyncTransaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReactivePgAsyncTransaction.class);

    private Connection transaction;
    private final Configuration configuration;

    public ReactivePgAsyncTransaction(Connection connection, Configuration configuration) {
        super(configuration == null ? DSL.using(connection) : DSL.using(connection, configuration.dialect()));
        this.transaction = connection;
        this.configuration = configuration;
    }

    @Override
    public CompletionStage<Tuple0> commit() {
        return Mono.from(transaction.commitTransaction()).thenReturn(Tuple.empty()).toFuture();
    }

    @Override
    public CompletionStage<Tuple0> rollback() {
        return Mono.from(transaction.rollbackTransaction()).thenReturn(Tuple.empty()).toFuture();
    }
}
