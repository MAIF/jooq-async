package fr.maif.jooq.reactor;

import reactor.core.publisher.Mono;


public interface PgAsyncTransaction extends PgAsyncClient, fr.maif.jooq.PgAsyncTransaction {

    Mono<Void> commitMono();
    Mono<Void> rollbackMono();

}
