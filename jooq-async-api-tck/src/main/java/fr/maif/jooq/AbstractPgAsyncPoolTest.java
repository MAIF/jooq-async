package fr.maif.jooq;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import io.vavr.jackson.datatype.VavrModule;

import static io.vavr.API.Some;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jooq.impl.DSL.field;

public abstract class AbstractPgAsyncPoolTest {

    private final static ObjectMapper mapper = new ObjectMapper();
    {
        mapper.registerModule(new VavrModule());
    }
    protected ActorSystem actorSystem = ActorSystem.create("test", ConfigFactory.parseString(
            "jdbc-execution-context {\n" +
            "  type = Dispatcher\n" +
            "  executor = \"thread-pool-executor\"\n" +
            "  throughput = 1\n" +
            "  thread-pool-executor {\n" +
            "    fixed-pool-size = 5\n" +
            "  }\n" +
            "}"));
    protected final static String user = "eventsourcing";
    protected final static String password = "eventsourcing";
    protected final static String database = "eventsourcing";
    protected final static Integer port = 5557;
    protected final static String url = "jdbc:postgresql://localhost:"+port+"/"+database;
    protected PGSimpleDataSource dataSource;
    protected DSLContext dslContext;
    protected final static AtomicReference<PGSimpleDataSource> dataSourceRef = new AtomicReference<>();

    public abstract PgAsyncPool pgAsyncPool();

    private Random seq = new Random();

    protected DataType<JsonNode> JSON = SQLDataType.JSONB.asConvertedDataType(new JsonConverter());

    protected Integer number;
    protected Table<Record> table;
    protected Field<String> name = field("name", String.class);
    protected Field<JsonNode> meta = field("meta", JSON);
    protected Field<Timestamp> created = field("created", Timestamp.class);

    @BeforeClass
    public static void initDS() {
        if (dataSourceRef.get() == null) {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setUrl(url);
            dataSource.setUser(user);
            dataSource.setPassword(password);
            dataSourceRef.set(dataSource);
        }
    }

    @Before
    public void setUp() {
        this.number = seq.nextInt(10000);
        this.table = DSL.table("viking_async" + number);
        this.dataSource = dataSourceRef.get();
        this.dslContext = DSL.using(dataSource, SQLDialect.POSTGRES, new Settings());
        try {
            dslContext.createTableIfNotExists(this.table)
                    .column(name)
                    .column(meta)
                    .column(created)
                    .execute();

            dslContext.createTableIfNotExists(Person.PERSON)
                    .column(Person.PERSON.NOM)
                    .column(Person.PERSON.METADATA)
                    .column(Person.PERSON.CREATED)
                    .execute();
        } catch(Exception e) {}
    }

    @After
    public void cleanUp() {
        try {
            this.dslContext.dropTable(table).execute();
        } catch (Exception e) {}
        try {
            this.dslContext.dropTable(Person.PERSON).execute();
        } catch (Exception e) {}
    }

    @Test
    public void insertInTransaction() {
        Future<Integer> insertResult = pgAsyncPool().inTransaction(t ->
                t.execute(dsl -> dsl.insertInto(table).set(name, "test"))
        );
        insertResult.get();

        List<QueryResult> result = pgAsyncPool().query(dsl -> dsl.select(name).from(table)).get();
        assertThat(result).hasSize(1);
    }

    @Test
    public void insertInTransactionWithRollback() {
        assertThatThrownBy(() ->
                pgAsyncPool().inTransaction(t -> t
                        .execute(dsl -> dsl.insertInto(table).set(name, "test"))
                        .mapTry(__ -> { throw new RuntimeException("Oups"); })
                )
                .get()
        ).hasMessage("Oups");

        List<QueryResult> result = pgAsyncPool().query(dsl -> dsl.select(name).from(table)).get();
        assertThat(result).isEmpty();
    }

    @Test
    public void insertManualTransaction() {
        pgAsyncPool().begin().flatMap(t -> t
                .execute(dsl -> dsl.insertInto(table).set(name, "test"))
                .onSuccess(__ -> t.commit())
                .onFailure(__ -> t.rollback())
        ).get();

        List<QueryResult> result = pgAsyncPool().query(dsl -> dsl.select(name).from(table)).get();
        assertThat(result).hasSize(1);
    }

    @Test
    public void insertManualTransactionWithRollback() {
        assertThatThrownBy(() ->
                pgAsyncPool().begin().flatMap(t -> t
                        .execute(dsl -> dsl.insertInto(table).set(name, "test"))
                        .mapTry(__ -> { throw new RuntimeException("Oups"); })
                        .onSuccess(__ -> t.commit())
                        .onFailure(__ -> t.rollback())
                ).get()
        ).hasMessage("Oups");

        List<QueryResult> result = pgAsyncPool().query(dsl -> dsl.select(name).from(table)).get();
        assertThat(result).isEmpty();
    }

    @Test
    public void executeBatchAndReadMany() {
        List<String> names = List.range(0, 10).map(i -> "name-" + i);
        Future<Long> batchResult = pgAsyncPool().executeBatch(
                dsl -> dslContext.insertInto(table).columns(name).values((String) null),
                names.map(List::of)
        );
        batchResult.get();
        List<String> result = pgAsyncPool().query(dsl -> dsl.select(name).from(table)).get().map(q -> q.get(name));
        assertThat(result).containsExactly(names.toJavaArray(String[]::new));
    }

    @Test
    public void executeBatch2AndReadMany() {
        List<String> names = List.range(0, 10).map(i -> "name-" + i);
        Future<Long> batchResult = pgAsyncPool().executeBatch(dsl ->
                names.map(n -> dslContext.insertInto(table).set(name, n))
        );
        batchResult.get();
        List<String> result = pgAsyncPool().query(dsl -> dsl.select(name).from(table)).get().map(q -> q.get(name));
        assertThat(result).containsExactly(names.toJavaArray(String[]::new));
    }

    @Test
    public void queryOne() {
        pgAsyncPool().executeBatch(dsl ->
                List.range(0, 10).map(i -> "name-" + i).map(n -> dslContext.insertInto(table).set(name, n))
        ).get();

Future<Option<String>> futureResult = pgAsyncPool()
        .queryOne(dsl -> dsl.select(name).from(table).where(name.eq("name-1")))
        .map(mayBeResult -> mayBeResult.map(row -> row.get(name)));
        Option<String> res = futureResult
                .get();
        assertThat(res).isEqualTo(Some("name-1"));
    }

    @Test
    public void queryOneEmpty() {
        Option<String> res = pgAsyncPool().queryOne(dsl -> dsl.select(name).from(table).where(name.eq("name-1"))).get().map(q -> q.get(name));
        assertThat(res).isEmpty();
    }

    @Test
    public void queryOneAsJson() {


        pgAsyncPool().executeBatch(dsl ->
                List.range(0, 10).map(i -> dslContext
                        .insertInto(table)
                        .set(name, "name-"+i)
                        .set(meta, jsonFromMap(HashMap.of("name", "A name "+i)))
                )
        ).get();

        Future<Option<JsonNode>> futureResult = pgAsyncPool()
                .queryOne(dsl -> dsl.select(meta).from(table).where(name.eq("name-1")))
                .map(mayBeResult -> mayBeResult.map(row -> row.get(meta)));

        Option<JsonNode> res = futureResult.get();
        assertThat(res).isEqualTo(Some(jsonFromMap(HashMap.of("name", "A name 1"))));
    }

    @Test
    public void queryOneAsTimestamp() {
        LocalDateTime localDateTime = LocalDateTime.of(2019, 1, 1, 0, 0, 0, 0);
        pgAsyncPool().executeBatch(dsl ->
                List.range(0, 10).map(i -> dslContext
                        .insertInto(table)
                        .set(name, "name-"+i)
                        .set(meta, jsonFromMap(HashMap.of("name", "A name "+i)))
                        .set(created, Timestamp.valueOf(localDateTime))
                )
        ).get();

        Future<Option<Timestamp>> futureResult = pgAsyncPool()
                .queryOne(dsl -> dsl.select(created).from(table).where(name.eq("name-1")))
                .map(mayBeResult -> mayBeResult.map(row -> row.get(created)));

        Option<Timestamp> res = futureResult.get();
        assertThat(res.map(Timestamp::toLocalDateTime)).isEqualTo(Some(localDateTime));
    }


    @Test
    public void stream() {
        List<String> names = List.range(0, 10000).map(i -> "name-" + i);
        pgAsyncPool().executeBatch(dsl ->
                names.map(n -> dslContext.insertInto(table).set(name, n))
        ).get();

        Source<String, NotUsed> stream = pgAsyncPool()
                .stream(10, dsl -> dsl.select(name).from(table))
                .map(q -> q.get(name));
        List<String> res = stream
                .runWith(Sink.seq(), Materializer.createMaterializer(actorSystem))
                .thenApply(List::ofAll)
                .toCompletableFuture().join();
        assertThat(res).containsExactlyInAnyOrder(names.toJavaArray(String[]::new));
    }

    @Test
    public void toRecordWithTable() {
        PersonRecord record = new PersonRecord(Person.PERSON);
        record.value1("JP revient");
        record.value2(org.jooq.JSON.valueOf("{\"foo\":\"bar\"}"));

        dslContext.insertInto(Person.PERSON)
                .set(record)
                .execute();

        final PersonRecord result = pgAsyncPool().query(dsl -> dsl.selectFrom(Person.PERSON))
                .get()
                .head()
                .toRecord(Person.PERSON);

        assertThat(result.component1()).isEqualTo(record.component1());
        assertThat(result.component2()).isEqualTo(record.component2());
    }

    @Test
    public void toRecordWithRecord() {
        PersonRecord record = new PersonRecord(Person.PERSON);
        record.value1("JP revient");
        record.value2(org.jooq.JSON.valueOf("{\"foo\":\"bar\"}"));

        dslContext.insertInto(Person.PERSON)
                .set(record)
                .execute();

        final PersonRecord result = pgAsyncPool().query(dsl -> dsl.selectFrom(Person.PERSON))
                .get()
                .head()
                .toRecord(new PersonRecord(Person.PERSON));

        assertThat(result.component1()).isEqualTo(record.component1());
        assertThat(result.component2()).isEqualTo(record.component2());
    }

    private JsonNode jsonFromMap(Map<String, Object> obj) {
        return mapper.convertValue(obj, ObjectNode.class);
    }
}
