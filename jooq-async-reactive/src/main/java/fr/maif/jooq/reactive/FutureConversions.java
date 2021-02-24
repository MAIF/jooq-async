package fr.maif.jooq.reactive;


public class FutureConversions {

    public static <T> io.vertx.core.Future<T> toVertx(io.vavr.concurrent.Future<T> vavrFuture) {
        io.vertx.core.Promise<T> promise = io.vertx.core.Promise.promise();
        vavrFuture.onSuccess(promise::complete);
        vavrFuture.onFailure(promise::fail);
        return promise.future();
    }

    public static <T> io.vavr.concurrent.Future<T> fromVertx(io.vertx.core.Future<T> vertxFuture) {
        io.vavr.concurrent.Promise<T> promise = io.vavr.concurrent.Promise.make();
        vertxFuture.onSuccess(promise::success);
        vertxFuture.onFailure(promise::failure);
        return promise.future();
    }

}
