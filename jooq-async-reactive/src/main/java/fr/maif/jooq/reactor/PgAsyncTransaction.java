package fr.maif.jooq.reactor;

import io.vavr.Tuple0;
import reactor.core.publisher.Mono;


public interface PgAsyncTransaction extends PgAsyncClient, fr.maif.jooq.PgAsyncTransaction {

    Mono<Tuple0> commitMono();
    Mono<Tuple0> rollbackMono();

}
