package fr.maif.jooq.reactor;

import reactor.core.publisher.Mono;

public interface PgAsyncConnection extends PgAsyncClient, fr.maif.jooq.PgAsyncConnection {

    Mono<Void> closeMono();
    Mono<PgAsyncTransaction> beginMono();
}
