

# Reactive jooq API [![ga-badge][]][ga] [![jar-badge][]][jar]

[ga]:               https://github.com/MAIF/jooq-async/actions?query=workflow%3ABuild
[ga-badge]:         https://github.com/MAIF/jooq-async/workflows/Build/badge.svg
[jar]:              https://maven-badges.herokuapp.com/maven-central/fr.maif/jooq-async-api
[jar-badge]:        https://maven-badges.herokuapp.com/maven-central/fr.maif/jooq-async-api/badge.svg

This API is a solution to use jooq with reactive clients for RDBMS.  

## Implementations 

At the moment there are 2 implementations: 
 * a blocking jdbc implementation 
 * a vertx reactive implementation for postgresql only 

## Import

Jcenter hosts this library.

### Maven

```xml
<dependency>
  <groupId>fr.maif</groupId>
  <artifactId>jooq-async-jdbc</artifactId>
    <version>${version}</version>
</dependency>
```

OR

```xml
<dependency>
  <groupId>fr.maif</groupId>
  <artifactId>jooq-async-reactive</artifactId>
  <version>${version}</version>
</dependency>
``` 

### Gradle

```gradle
implementation "fr.maif:jooq-async-api:${version}"
```

OR

```gradle
implementation "fr.maif:jooq-async-reactive:${version}"
```

## The API 

### Create a pool 

The JDBC one : 

```java
PGSimpleDataSource dataSource = new PGSimpleDataSource();
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
Pool client = PgBuilder.pool()
        .using(vertx)
        .connectingTo(options)
        .with(poolOptions)
        .build();

PgAsyncPool reactivePgAsyncPool = new ReactivePgAsyncPool(client, jooqConfig);
```

### Perform query 

The idea is to use the jooq DSL as a builder to write the query. The query is then run against the underlying library.  

#### Query one : 

```java
CompletionStage<Option<String>> futureResult = reactivePgAsyncPool
        .queryOne(dsl -> dsl.select(name).from(table).where(name.eq("Ragnar")))
        .map(mayBeResult -> mayBeResult.map(row -> row.get(name)));
```

#### Query many : 

```java
CompletionStage<List<String>> futureResult = reactivePgAsyncPool
        .query(dsl -> dsl.select(name).from(table)))
        .map(results -> results.map(row -> row.get(name)));
```

#### Stream data 

```java
Publisher<String, NotUsed> stream = reactivePgAsyncPool
                .stream(500 /*fetch size*/, dsl -> dsl.select(name).from(table))
                .map(q -> q.get(name));
```

The publisher comes from the reactive streams API. 

#### Execute statement

```java 
CompletionStage<Integer> insertResult = reactivePgAsyncPool.inTransaction(t ->
        t.execute(dsl -> dsl.insertInto(table).set(name, "test"))
);
``` 

#### Batch statements

With this version you can send a statement once and then send all parameters. 
This version is the most performant if you have one statement with multiple values. 

```java
List<String> names = List.range(0, 10).map(i -> "name-" + i);
CompletionStage<Long> batchResult = reactivePgAsyncPool.executeBatch(
        dsl -> dslContext.insertInto(table).columns(name).values((String) null),
        names.map(List::of)
);
```

With this version, you can batch a set of statements. You should use this version if your statements are all different. 

```java
List<String> names = List.range(0, 10).map(i -> "name-" + i);
CompletionStage<Long> batchResult = reactivePgAsyncPool.executeBatch(dsl ->
        names.map(n -> dslContext.insertInto(table).set(name, n))
);
```

## Spring reactor :

The `jooq-async-reactive` module expose operations with the `Mono` / `Flux` API. 

```java
PgAsyncPool pgAsyncPool = PgAsyncPool.create(client, jooqConfig);

Mono<Option<String>> result =  pgAsyncPool.queryOneOne(dsl -> dsl
            .select(name)
            .from(table)
            .where(name.eq("Ragnar"))
        )
        .map(mayBeResult -> mayBeResult.map(row -> row.get(name)));
```