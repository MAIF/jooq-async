package fr.maif.jooq;

import io.vavr.Tuple0;
import io.vavr.concurrent.Future;

public interface PgAsyncConnection extends PgAsyncClient {

    Future<Tuple0> close();
    Future<PgAsyncTransaction> begin();
}
