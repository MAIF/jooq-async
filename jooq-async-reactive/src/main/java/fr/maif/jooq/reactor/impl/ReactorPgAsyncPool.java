package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.PgAsyncPool;
import fr.maif.jooq.reactor.PgAsyncConnection;
import fr.maif.jooq.reactor.PgAsyncTransaction;
import reactor.core.publisher.Mono;

public class ReactorPgAsyncPool extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncPool {

    private final PgAsyncPool underlying;

    public ReactorPgAsyncPool(PgAsyncPool underlying) {
        super(underlying);
        this.underlying = underlying;
    }

    @Override
    public Mono<PgAsyncConnection> connection() {
        return Mono.fromCompletionStage(this.underlying::connection)
                .map(c -> new ReactorPgAsyncConnection(c, super.underlying));
    }

    @Override
    public Mono<PgAsyncTransaction> begin() {
        return Mono.fromCompletionStage(this.underlying::begin)
                .map(t -> new ReactorPgAsyncTransaction(super.underlying, t));
    }
}
