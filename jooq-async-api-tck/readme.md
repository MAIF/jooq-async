# A test kit for `jooq-async-api` 

This test kit can be used to validate a `jooq-async-api` implementation.

```java

public class CustomReactiveAsyncPoolTest extends AbstractPgAsyncPoolTest {

    @Override
    public PgAsyncPool pgAsyncPool() {
        return ...; // return instance here 
    }
}
```