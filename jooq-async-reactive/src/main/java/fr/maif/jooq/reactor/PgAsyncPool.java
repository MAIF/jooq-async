package fr.maif.jooq.reactor;

import fr.maif.jooq.PgAsyncPoolGetter;

import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface PgAsyncPool extends PgAsyncClient, fr.maif.jooq.PgAsyncPool, PgAsyncPoolGetter {

    Mono<PgAsyncConnection> connectionMono();

    Mono<PgAsyncTransaction> beginMono();

    <T> Mono<T> inTransactionMono(Function<PgAsyncTransaction, Mono<T>> action);

    fr.maif.jooq.PgAsyncPool pgAsyncPool();

}
