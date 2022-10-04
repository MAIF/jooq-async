package fr.maif.jooq.reactor;

import io.vavr.Tuple0;
import reactor.core.publisher.Mono;

public interface PgAsyncConnection extends PgAsyncClient, fr.maif.jooq.PgAsyncConnection {

    Mono<Tuple0> closeMono();
    Mono<PgAsyncTransaction> beginMono();
}
