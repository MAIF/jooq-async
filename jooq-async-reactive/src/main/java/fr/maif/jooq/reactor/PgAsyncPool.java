package fr.maif.jooq.reactor;

import fr.maif.jooq.PgAsyncPoolGetter;
import fr.maif.jooq.reactive.ReactivePgAsyncPool;
import fr.maif.jooq.reactor.impl.ReactorPgAsyncClient;
import fr.maif.jooq.reactor.impl.ReactorPgAsyncPool;
import io.vertx.pgclient.PgPool;
import org.jooq.Configuration;

import io.vertx.sqlclient.Pool;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface PgAsyncPool extends PgAsyncClient, fr.maif.jooq.PgAsyncPool, PgAsyncPoolGetter {

    Mono<PgAsyncConnection> connectionMono();

    Mono<PgAsyncTransaction> beginMono();

    static PgAsyncPool create(Pool client, Configuration configuration) {
        ReactivePgAsyncPool pool = new ReactivePgAsyncPool(client, configuration);
        return new ReactorPgAsyncPool(pool);
    }

    static PgAsyncPool create(ReactivePgAsyncPool pgAsyncPool) {
        return new ReactorPgAsyncPool(pgAsyncPool);
    }

    <T> Mono<T> inTransactionMono(Function<PgAsyncTransaction, Mono<T>> action);

    fr.maif.jooq.PgAsyncPool pgAsyncPool();




}
