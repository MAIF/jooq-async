package fr.maif.jooq;

import java.util.concurrent.CompletionStage;

public interface PgAsyncTransaction extends PgAsyncClient {

    CompletionStage<Void> commit();
    CompletionStage<Void> rollback();

}
