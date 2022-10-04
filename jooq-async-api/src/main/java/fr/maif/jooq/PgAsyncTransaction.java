package fr.maif.jooq;

import io.vavr.Tuple0;

import java.util.concurrent.CompletionStage;

public interface PgAsyncTransaction extends PgAsyncClient {

    CompletionStage<Tuple0> commit();
    CompletionStage<Tuple0> rollback();

}
