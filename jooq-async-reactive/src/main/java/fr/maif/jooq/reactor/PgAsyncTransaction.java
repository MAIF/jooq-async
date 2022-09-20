package fr.maif.jooq.reactor;

import reactor.core.publisher.Mono;


public interface PgAsyncTransaction extends PgAsyncClient {

    Mono<Void> commit();
    Mono<Void> rollback();

}
