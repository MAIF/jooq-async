package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncConnection;
import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.reactor.PgAsyncTransaction;
import reactor.core.publisher.Mono;

public class ReactorPgAsyncConnection extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncConnection {

    private final PgAsyncConnection underlying;

    public ReactorPgAsyncConnection(PgAsyncConnection underlying, PgAsyncClient pgAsyncClient) {
        super(pgAsyncClient);
        this.underlying = underlying;
    }

    @Override
    public Mono<Void> close() {
        return Mono.fromCompletionStage(this.underlying::close);
    }

    @Override
    public Mono<PgAsyncTransaction> begin() {
        return Mono.fromCompletionStage(this.underlying::begin).map(t -> new ReactorPgAsyncTransaction(this.underlying, t));
    }
}
