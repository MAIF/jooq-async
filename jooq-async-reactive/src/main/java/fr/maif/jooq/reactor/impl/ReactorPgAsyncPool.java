package fr.maif.jooq.reactor.impl;

import fr.maif.jooq.reactor.PgAsyncConnection;
import fr.maif.jooq.reactor.PgAsyncTransaction;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class ReactorPgAsyncPool extends ReactorPgAsyncClient implements fr.maif.jooq.reactor.PgAsyncPool, fr.maif.jooq.PgAsyncPool {

    private final fr.maif.jooq.PgAsyncPool underlying;

    public ReactorPgAsyncPool(fr.maif.jooq.PgAsyncPool underlying) {
        super(underlying);
        this.underlying = underlying;
    }

    @Override
    public <T> Mono<T> inTransactionMono(Function<PgAsyncTransaction, Mono<T>> action) {
        return Mono.fromCompletionStage(() -> underlying.inTransaction(tx ->
                action.apply(new ReactorPgAsyncTransaction(tx)).toFuture()
        ));
    }

    @Override
    public Mono<PgAsyncConnection> connectionMono() {
        return Mono.fromCompletionStage(this.underlying::connection)
                .map(ReactorPgAsyncConnection::new);
    }

    @Override
    public Mono<PgAsyncTransaction> beginMono() {
        return Mono.fromCompletionStage(this.underlying::begin)
                .map(ReactorPgAsyncTransaction::new);
    }

    @Override
    public fr.maif.jooq.PgAsyncPool pgAsyncPool() {
        return underlying;
    }

    @Override
    public CompletionStage<fr.maif.jooq.PgAsyncConnection> connection() {
        return underlying.connection();
    }

    @Override
    public CompletionStage<fr.maif.jooq.PgAsyncTransaction> begin() {
        return underlying.begin();
    }

    @Override
    public <T> CompletionStage<T> inTransaction(Function<fr.maif.jooq.PgAsyncTransaction, CompletionStage<T>> action) {
        return underlying.inTransaction(action);
    }
}
