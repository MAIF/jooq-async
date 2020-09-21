# Reactive jooq API 

This API is a solution to use jooq with reactive client for RDBMS.  

## Implementations 

At the moment there is 2 implementations : 
 * a blocking jdbc implementation 
 * a vertx reactive implementation for postgresql only 

## The API 

### Create a pool 

The JDBC one : 

```java
DataSource dataSource = dataSource = new PGSimpleDataSource();
dataSource.setUrl(url);
dataSource.setUser(user);
dataSource.setPassword(password);
PgAsyncPool jdbcPgAsyncPool = new JdbcPgAsyncPool(SQLDialect.POSTGRES, dataSource, Executors.newFixedThreadPool(5));
```

The reactive one : 

```java
DefaultConfiguration jooqConfig = new DefaultConfiguration();
jooqConfig.setSQLDialect(SQLDialect.POSTGRES);
PgConnectOptions options = new PgConnectOptions()
        .setPort(port)
        .setHost(host)
        .setDatabase(database)
        .setUser(user)
        .setPassword(password);
PoolOptions poolOptions = new PoolOptions().setMaxSize(10);
Vertx vertx = Vertx.vertx();
PgPool client = PgPool.pool(vertx, options, poolOptions);

PgAsyncPool reactivePgAsyncPool = new ReactivePgAsyncPool(client, jooqConfig);
```

### Perform query 

The idea is to use the jooq DSL as a builder to write the query. The query is then run against the underlying library.  

#### Query one : 

```java
Future<Option<String>> futureResult = reactivePgAsyncPool
        .queryOne(dsl -> dsl.select(name).from(table).where(name.eq("Ragnar")))
        .map(mayBeResult -> mayBeResult.map(row -> row.get(name)));
```

#### Query many : 

```java
Future<List<String>> futureResult = reactivePgAsyncPool
        .query(dsl -> dsl.select(name).from(table)))
        .map(results -> results.map(row -> row.get(name)));
```

#### Stream data 

```java
Source<String, NotUsed> stream = reactivePgAsyncPool
                .stream(500 /*fetch size*/, dsl -> dsl.select(name).from(table))
                .map(q -> q.get(name));
```

#### Execute statement

```java 
Future<Integer> insertResult = reactivePgAsyncPool.inTransaction(t ->
        t.execute(dsl -> dsl.insertInto(table).set(name, "test"))
);
``` 

#### Batch statements

```java
List<String> names = List.range(0, 10).map(i -> "name-" + i);
Future<Long> batchResult = reactivePgAsyncPool.executeBatch(
        dsl -> dslContext.insertInto(table).columns(name).values((String) null),
        names.map(List::of)
);
```

Or 

```java
List<String> names = List.range(0, 10).map(i -> "name-" + i);
Future<Long> batchResult = reactivePgAsyncPool.executeBatch(dsl ->
        names.map(n -> dslContext.insertInto(table).set(name, n))
);
```
