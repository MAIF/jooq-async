package fr.maif.jooq.r2bdc;

import fr.maif.jooq.QueryResult;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

public record JooqQueryResult(Record record) implements QueryResult {
    @Override
    public <T> T get(Field<T> field) {
        return record.get(field);
    }

    @Override
    public <T> T get(int index, Class<T> type) {
        return record.get(index, type);
    }

    @Override
    public <T> T get(int index, Field<T> field) {
        return record.get(index, field.getConverter());
    }

    @Override
    public <T> T get(String columnName, Class<T> type) {
        if (type.isAssignableFrom(String.class)) {

        }
        return record.get(columnName, type);
    }

    @Override
    public <T extends org.jooq.Record> T toRecord(Table<T> table) {
        return (T) record;
    }

    @Override
    public <T extends org.jooq.Record> T toRecord(T record) {
        return (T) record;
    }
}
