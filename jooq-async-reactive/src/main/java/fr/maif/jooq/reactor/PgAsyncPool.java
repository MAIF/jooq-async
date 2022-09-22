package fr.maif.jooq.reactor;

import fr.maif.jooq.PgAsyncPoolGetter;
import fr.maif.jooq.reactive.ReactivePgAsyncPool;
import fr.maif.jooq.reactor.impl.ReactorPgAsyncClient;
import fr.maif.jooq.reactor.impl.ReactorPgAsyncPool;
import io.vertx.pgclient.PgPool;
import org.jooq.Configuration;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface PgAsyncPool extends PgAsyncClient, fr.maif.jooq.PgAsyncClient, PgAsyncPoolGetter {

    Mono<PgAsyncConnection> connectionMono();

    Mono<PgAsyncTransaction> beginMono();

    static PgAsyncPool create(PgPool client, Configuration configuration) {
        ReactivePgAsyncPool pool = new ReactivePgAsyncPool(client, configuration);
        return new ReactorPgAsyncPool(pool);
    }

    static PgAsyncPool create(ReactivePgAsyncPool pgAsyncPool) {
        return new ReactorPgAsyncPool(pgAsyncPool);
    }

    default <T> Mono<T> inTransactionMono(Function<PgAsyncTransaction, Mono<T>> action) {
        return beginMono().flatMap(t ->
                action.apply(t)
                        .flatMap(r -> t.commitMono().map(__ -> r))
                        .onErrorResume(e ->
                                t.rollbackMono().flatMap(__ -> Mono.error(e))
                        )
        );
    }

    fr.maif.jooq.PgAsyncPool pgAsyncPool();




}
