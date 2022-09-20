package fr.maif.jooq.reactor;

import reactor.core.publisher.Mono;

public interface PgAsyncConnection extends PgAsyncClient {

    Mono<Void> close();
    Mono<PgAsyncTransaction> begin();
}
