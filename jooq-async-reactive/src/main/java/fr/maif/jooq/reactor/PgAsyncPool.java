package fr.maif.jooq.reactor;

import fr.maif.jooq.reactive.ReactivePgAsyncPool;
import fr.maif.jooq.reactor.impl.ReactorPgAsyncClient;
import fr.maif.jooq.reactor.impl.ReactorPgAsyncPool;
import io.vertx.pgclient.PgPool;
import org.jooq.Configuration;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface PgAsyncPool extends PgAsyncClient {

    Mono<PgAsyncConnection> connection();

    Mono<PgAsyncTransaction> begin();

    static PgAsyncPool create(PgPool client, Configuration configuration) {
        ReactivePgAsyncPool pool = new ReactivePgAsyncPool(client, configuration);
        return new ReactorPgAsyncPool(pool);
    }

    static PgAsyncPool create(ReactivePgAsyncPool pgAsyncPool) {
        return new ReactorPgAsyncPool(pgAsyncPool);
    }

    default <T> Mono<T> inTransaction(Function<PgAsyncTransaction, Mono<T>> action) {
        return begin().flatMap(t ->
                action.apply(t)
                        .flatMap(r -> t.commit().map(__ -> r))
                        .onErrorResume(e ->
                                t.rollback().flatMap(__ -> Mono.error(e))
                        )
        );
    }

}
