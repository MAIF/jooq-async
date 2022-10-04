package fr.maif.jooq;

import io.vavr.Tuple0;

import java.util.concurrent.CompletionStage;

public interface PgAsyncConnection extends PgAsyncClient {

    CompletionStage<Tuple0> close();
    CompletionStage<PgAsyncTransaction> begin();
}
