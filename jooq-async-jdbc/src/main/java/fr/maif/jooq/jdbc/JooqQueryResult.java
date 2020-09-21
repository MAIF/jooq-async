package fr.maif.jooq.jdbc;

import fr.maif.jooq.QueryResult;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public class JooqQueryResult implements QueryResult {

    Record jooqResult;

    public JooqQueryResult(Record jooqResult) {
        this.jooqResult = jooqResult;
    }

    @Override
    public <T> T get(Field<T> field) {
        return jooqResult.get(field);
    }

    @Override
    public <T> T get(int index, Class<T> type) {
        return jooqResult.get(index, type);
    }

    @Override
    public <T> T get(int index, Field<T> field) {
        return jooqResult.get(index, field.getConverter());
    }

    @Override
    public <T> T get(String columnName, Class<T> type) {
        return jooqResult.get(columnName, type);
    }

    public <T extends Record> T toRecord(Table<T> table) {
        return jooqResult.into(table);
    }

    public <T extends Record> T toRecord(T record) {
        return jooqResult.into(record);
    }
}
