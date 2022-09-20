package fr.maif.jooq;

import io.vavr.Tuple0;
import io.vavr.concurrent.Future;

import java.util.concurrent.CompletionStage;

public interface PgAsyncConnection extends PgAsyncClient {

    CompletionStage<Void> close();
    CompletionStage<PgAsyncTransaction> begin();
}
