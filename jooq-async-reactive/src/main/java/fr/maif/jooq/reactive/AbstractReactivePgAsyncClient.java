package fr.maif.jooq.reactive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import fr.maif.jooq.PgAsyncClient;
import fr.maif.jooq.QueryResult;
import io.vavr.collection.List;
import io.vavr.concurrent.Future;
import io.vavr.concurrent.Promise;
import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.ArrayTuple;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.conf.ParamType;
import org.jooq.exception.TooManyRowsException;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static fr.maif.jooq.reactive.FutureConversions.fromVertx;
import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.util.Objects.isNull;

public abstract class AbstractReactivePgAsyncClient<Client extends SqlClient> implements PgAsyncClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractReactivePgAsyncClient.class);

    private static final String BIND_VALUE_REPLACEMENT_PATTERN = "(?<!:):(?![:\\*])";
    protected final Client client;
    protected final Configuration configuration;
    protected final ObjectMapper mapper = new ObjectMapper();

    public AbstractReactivePgAsyncClient(Client client, Configuration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    protected <R extends Record> CompletionStage<RowSet<Row>> rawPreparedQuery(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        Query query = createQuery(queryFunction);
        log(query);
        String preparedQuery = toPreparedQuery(query);
        Tuple bindValues = getBindValues(query);
        return fromVertx(client.preparedQuery(preparedQuery).execute(bindValues));
    }

    @Override
    public <R extends Record> CompletionStage<Option<QueryResult>> queryOne(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return rawPreparedQuery(queryFunction).thenCompose(res -> {
            switch (res.size()) {
                case 0:
                    return completedStage(Option.none());
                case 1:
                    return completedStage(Option.of(new ReactiveRowQueryResult(res.iterator().next())));
                default:
                    return failedStage(new TooManyRowsException(String.format("Found more than one row: %d", res.size())));
            }
        });
    }

    @Override
    public <R extends Record> CompletionStage<List<QueryResult>> query(Function<DSLContext, ? extends ResultQuery<R>> queryFunction) {
        return rawPreparedQuery(queryFunction).thenApply(AbstractReactivePgAsyncClient::asList);
    }

    @Override
    public CompletionStage<Integer> execute(Function<DSLContext, ? extends Query> queryFunction) {
        Query query = createQuery(queryFunction);
        log(query);
        return fromVertx(client.preparedQuery(toPreparedQuery(query)).execute(getBindValues(query)))
                .thenApply(RowSet::rowCount);
    }

    @Override
    public CompletionStage<Long> executeBatch(Function<DSLContext, List<? extends Query>> queryFunction) {
        List<? extends Query> queries = queryFunction.apply(DSL.using(configuration));
        if (queries.isEmpty()) {
            return completedStage(0L);
        }
        return queries.foldLeft(completedStage(0L), (acc, query) ->
                acc.thenCompose(count -> {
                    log(query);
                    String preparedQuery = toPreparedQuery(query);
                    Tuple bindValues = getBindValues(query);
                    return fromVertx(client.preparedQuery(preparedQuery).execute(bindValues))
                            .thenApply(RowSet::rowCount)
                            .thenApply(c -> count + c);
                })
        );
    }

    @Override
    public CompletionStage<Long> executeBatch(Function<DSLContext, ? extends Query> queryFunction, List<List<Object>> values) {
        if (values.isEmpty()) {
            return completedStage(0L);
        }
        CompletableFuture<RowSet<Row>> rowFuture = new CompletableFuture<>();
        try {
            Query query = queryFunction.apply(DSL.using(configuration));
            log(query);
            String preparedQuery = toPreparedQuery(query);
            List<Tuple> bindValues = values
                    .map(objects -> objects.map(o -> {
                        if (isNull(o)) {
                            return null;
                        }
                        try {
                            return convertToDatabaseType(o);
                        } catch (IOException e) {
                            throw new RuntimeException("error binding values", e);
                        }
                    }))
                    .map(l -> {
                        if (l.size() == 1) {
                            return Tuple.of(l.head());
                        } else {
                            return Tuple.of(l.head(), l.tail().toJavaArray(Object[]::new));
                        }
                    });
            client.preparedQuery(preparedQuery).executeBatch(bindValues.toJavaList(), toCompletionHandler(rowFuture));
        } catch (Exception e) {
            rowFuture.completeExceptionally(e);
        }
        return rowFuture.thenApply(r -> Option(r).flatMap(c -> Option(c.rowCount())).map(Integer::longValue).getOrElse(0L));
    }

    protected static <U> Handler<AsyncResult<U>> toCompletionHandler(CompletableFuture<U> future) {
        return h -> {
            if (h.succeeded()) {
                Try.of(h::result).fold(
                        future::completeExceptionally,
                        future::complete
                );
            } else {
                future.completeExceptionally(h.cause());
            }
        };
    }


    protected static List<QueryResult> asList(RowSet<Row> result) {
        return List.ofAll(StreamSupport
                .stream(result.spliterator(), false)
                .map(ReactiveRowQueryResult::new)
                .collect(Collectors.toList()));
    }

    protected <T extends Query> T createQuery(Function<DSLContext, T> queryFunction) {
        return queryFunction.apply(DSL.using(configuration));
    }


    protected Tuple getBindValues(Query query) {
        ArrayTuple bindValues = new ArrayTuple(query.getParams().size());
        for (Param<?> param : query.getParams().values()) {
            if (!param.isInline()) {
                Object value = convertParamToDatabaseType(param);
                bindValues.addValue(value);
            }
        }
        return bindValues;
    }

    protected Object convertToDatabaseType(Object obj) throws IOException {
        if (isNull(obj)) {
            return null;
        } else if (obj instanceof JSON) {
            JSON value = (JSON) obj;
            return jacksonToVertx(readJson(value.data()));
        } else if (obj instanceof JSONB) {
            JSONB value = (JSONB) obj;
            return jacksonToVertx(readJson(value.data()));
        } else if (obj instanceof JsonNode) {
            return jacksonToVertx((JsonNode) obj);
        } else {
            return obj;
        }
    }

    protected <U> Object convertParamToDatabaseType(Param<U> param) {
        /*
         * https://github.com/vertx/reactive-pg-client/issues/191 enum types are treated as unknown
         * DataTypes. Workaround is to convert them to string before adding to the Tuple.
         */
        if (Enum.class.isAssignableFrom(param.getBinding().converter().toType())) {
            return param.getValue().toString();
        } else if (param.getValue() instanceof Timestamp) {
            Timestamp value = (Timestamp) param.getValue();
            return value.toLocalDateTime();
        } else if (param.getDataType().equals(SQLDataType.JSON)) {
            JSON value = (JSON) param.getValue();
            return jacksonToVertx(readJson(value.data()));
        } else if (param.getDataType().equals(SQLDataType.JSONB)) {
            JSONB value = (JSONB) param.getValue();
            return jacksonToVertx(readJson(value.data()));
        } else if (param.getDataType().getTypeName().contains("json")) {
            if (param.getValue() instanceof JsonNode) {
                return jacksonToVertx((JsonNode) param.getValue());
            } else {
                String jsonString = param.getBinding().converter().to(param.getValue()).toString();
                JsonNode json = readJson(jsonString);
                return jacksonToVertx(json);
            }

        } else {
            return param.getBinding().converter().to(param.getValue());
        }
    }

    private Object jacksonToVertx(JsonNode json) {
        return Match(json).of(
                Case($(instanceOf(ObjectNode.class)), node -> {
                    Map<String, Object> t = mapper.convertValue(node, new TypeReference<Map<String, Object>>() {
                    });
                    return new JsonObject(t);
                }),
                Case($(instanceOf(ArrayNode.class)), array -> {
                    java.util.List<Object> t = mapper.convertValue(array, new TypeReference<java.util.List<Object>>() {
                    });
                    return new JsonArray(t);
                }),
                Case($(instanceOf(NullNode.class)), booleanNode ->
                        Tuple.JSON_NULL
                ),
                Case($(instanceOf(BooleanNode.class)), booleanNode ->
                        booleanNode.booleanValue()
                ),
                Case($(instanceOf(TextNode.class)), textNode ->
                        textNode.textValue()
                ),
                Case($(instanceOf(NumericNode.class)), numericNode ->
                        numericNode.numberValue()
                ),
                Case($(), other -> other)
        );
    }

    protected String toPreparedQuery(Query query) {
        String namedQuery = query.getSQL(ParamType.NAMED);
        return namedQuery.replaceAll(BIND_VALUE_REPLACEMENT_PATTERN, "\\$");
    }

    protected void log(Query query) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing {}", query.getSQL(ParamType.INLINED));
        }
    }

    JsonNode readJson(String json) {
        try {
            return mapper.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing json "+json, e);
        }
    }

    static <T> CompletionStage<T> completedStage(T value) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        cf.complete(value);
        return cf;
    }

    static <T> CompletionStage<T> failedStage(Throwable throwable) {
        CompletableFuture<T> cf = new CompletableFuture<>();
        cf.completeExceptionally(throwable);
        return cf;
    }
}
