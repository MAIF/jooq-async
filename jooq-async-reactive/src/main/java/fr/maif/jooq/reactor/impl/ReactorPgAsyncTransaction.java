package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.PgAsyncTransaction;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;

public class ReactorPgAsyncTransaction extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncTransaction {

    private final PgAsyncTransaction underlying;

    public ReactorPgAsyncTransaction(PgAsyncClient pgAsyncClient, PgAsyncTransaction pgAsyncTransaction) {
        super(pgAsyncClient);
        this.underlying = pgAsyncTransaction;
    }

    @Override
    public Mono<Void> commitMono() {
        return Mono.fromCompletionStage(this.underlying::commit);
    }

    @Override
    public Mono<Void> rollbackMono() {
        return Mono.fromCompletionStage(this.underlying::rollback);
    }

    @Override
    public CompletionStage<Void> commit() {
        return underlying.commit();
    }

    @Override
    public CompletionStage<Void> rollback() {
        return underlying.rollback();
    }
}
