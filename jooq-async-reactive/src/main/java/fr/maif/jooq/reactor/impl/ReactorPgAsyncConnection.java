package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.reactor.PgAsyncTransaction;
import io.vavr.Tuple0;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;

public class ReactorPgAsyncConnection extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncConnection, fr.maif.jooq.PgAsyncConnection {

    private final fr.maif.jooq.PgAsyncConnection underlying;

    public ReactorPgAsyncConnection(fr.maif.jooq.PgAsyncConnection underlying) {
        super(underlying);
        this.underlying = underlying;
    }

    @Override
    public Mono<Tuple0> closeMono() {
        return Mono.fromCompletionStage(this.underlying::close);
    }

    @Override
    public Mono<PgAsyncTransaction> beginMono() {
        return Mono.fromCompletionStage(this.underlying::begin).map(ReactorPgAsyncTransaction::new);
    }

    @Override
    public CompletionStage<Tuple0> close() {
        return underlying.close();
    }

    @Override
    public CompletionStage<fr.maif.jooq.PgAsyncTransaction> begin() {
        return underlying.begin();
    }
}
