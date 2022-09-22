package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.reactor.PgAsyncTransaction;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;

public class ReactorPgAsyncConnection extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncConnection, fr.maif.jooq.PgAsyncConnection {

    private final fr.maif.jooq.PgAsyncConnection underlying;

    public ReactorPgAsyncConnection(fr.maif.jooq.PgAsyncConnection underlying, PgAsyncClient pgAsyncClient) {
        super(pgAsyncClient);
        this.underlying = underlying;
    }

    @Override
    public Mono<Void> closeMono() {
        return Mono.fromCompletionStage(this.underlying::close);
    }

    @Override
    public Mono<PgAsyncTransaction> beginMono() {
        return Mono.fromCompletionStage(this.underlying::begin).map(t -> new ReactorPgAsyncTransaction(this.underlying, t));
    }

    @Override
    public CompletionStage<Void> close() {
        return underlying.close();
    }

    @Override
    public CompletionStage<fr.maif.jooq.PgAsyncTransaction> begin() {
        return underlying.begin();
    }
}
