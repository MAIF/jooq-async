package fr.maif.jooq.reactive;


import java.util.concurrent.CompletionStage;

public class FutureConversions {

    public static <T> io.vertx.core.Future<T> toVertx(CompletionStage<T> javaFuture) {
        return io.vertx.core.Future.fromCompletionStage(javaFuture);
    }

    public static <T> CompletionStage<T> fromVertx(io.vertx.core.Future<T> vertxFuture) {
        io.vavr.concurrent.Promise<T> promise = io.vavr.concurrent.Promise.make();
        vertxFuture.onSuccess(promise::success);
        vertxFuture.onFailure(promise::failure);
        return vertxFuture.toCompletionStage();
    }

}
