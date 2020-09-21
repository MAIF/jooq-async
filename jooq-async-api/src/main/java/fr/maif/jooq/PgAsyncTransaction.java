package fr.maif.jooq;

import io.vavr.Tuple0;
import io.vavr.concurrent.Future;

public interface PgAsyncTransaction extends PgAsyncClient {

    Future<Tuple0> commit();
    Future<Tuple0> rollback();

}
