package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.PgAsyncTransaction;
import io.vavr.Tuple0;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;

public class ReactorPgAsyncTransaction extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncTransaction {

    private final PgAsyncTransaction underlying;

    public ReactorPgAsyncTransaction(PgAsyncClient pgAsyncClient, PgAsyncTransaction pgAsyncTransaction) {
        super(pgAsyncClient);
        this.underlying = pgAsyncTransaction;
    }

    @Override
    public Mono<Tuple0> commitMono() {
        return Mono.fromCompletionStage(this.underlying::commit);
    }

    @Override
    public Mono<Tuple0> rollbackMono() {
        return Mono.fromCompletionStage(this.underlying::rollback);
    }

    @Override
    public CompletionStage<Tuple0> commit() {
        return underlying.commit();
    }

    @Override
    public CompletionStage<Tuple0> rollback() {
        return underlying.rollback();
    }
}
