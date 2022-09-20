package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.PgAsyncTransaction;
import reactor.core.publisher.Mono;

public class ReactorPgAsyncTransaction extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncTransaction {

    private final PgAsyncTransaction underlying;

    public ReactorPgAsyncTransaction(PgAsyncClient pgAsyncClient, PgAsyncTransaction pgAsyncTransaction) {
        super(pgAsyncClient);
        this.underlying = pgAsyncTransaction;
    }

    @Override
    public Mono<Void> commit() {
        return Mono.fromCompletionStage(this.underlying::commit);
    }

    @Override
    public Mono<Void> rollback() {
        return Mono.fromCompletionStage(this.underlying::rollback);
    }
}