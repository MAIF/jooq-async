package fr.maif.jooq.reactive;

import fr.maif.jooq.QueryResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.tools.Convert;

import java.util.Arrays;


public class ReactiveRowQueryResult implements QueryResult {

    private final Row current;

    public ReactiveRowQueryResult(Row row) {
        this.current = row;
    }

    @Override
    public <T> T get(Field<T> field) {
        return handleValue(current.getValue(field.getName()), field.getConverter());
    }

    @Override
    public <T> T get(int index, Class<T> type) {
        return Convert.convert(current.getValue(index), type);
    }

    @Override
    public <T> T get(int index, Field<T> field) {
        return handleValue(current.getValue(index), field.getConverter());
    }

    @Override
    public <T> T get(String columnName, Class<T> type) {
        return Convert.convert(current.getValue(columnName), type);
    }


    private <T> T handleValue(Object value, Converter<?, T> converter) {
        if (value == null) {
            return null;
        }
        if (value instanceof JsonArray) {
            if (converter != null && converter.fromType().equals(JSON.class)) {
                return Convert.convert( JSON.valueOf(((JsonArray) value).encode()), converter);
            } else if (converter != null && converter.fromType().equals(JSONB.class)) {
                return Convert.convert( JSONB.valueOf(((JsonArray) value).encode()), converter);
            }
        }
        if (value instanceof JsonObject) {
            if (converter != null && converter.fromType().equals(JSON.class)) {
                return Convert.convert( JSON.valueOf(((JsonObject) value).encode()), converter);
            } else if (converter != null && converter.fromType().equals(JSONB.class)) {
                return Convert.convert( JSONB.valueOf(((JsonObject) value).encode()), converter);
            }
        }

        // Quand on lit un champ json avec la valeur null (la valeur JSON nulle, pas celle de la BDD)
        // la valeur lue est juste un Object (pas une sous classe)
        if(value.getClass().equals(Object.class)) {
            return (T)JSON.valueOf("null");
        }

        return Convert.convert(value, converter);
    }

    public <T extends Record> T toRecord(Table<T> table) {
        return toRecord(table.newRecord());
    }

    public <T extends Record> T toRecord(T record) {
        Arrays.stream(record.fields())
            .forEach(field ->
                    record.set((Field)field, this.get(field))
            );

        return record;
    }
}
