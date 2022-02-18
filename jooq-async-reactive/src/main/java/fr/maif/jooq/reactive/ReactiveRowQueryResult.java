package fr.maif.jooq.reactive;

import fr.maif.jooq.QueryResult;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.jooq.tools.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
                Converter<JSON, T> converterCast = (Converter<JSON, T>) converter;
                JSON json = JSON.valueOf(((JsonArray) value).encode());
                return converterCast.from(json);
            } else if (converter != null && converter.fromType().equals(JSONB.class)) {
                Converter<JSONB, T> converterCast = (Converter<JSONB, T>) converter;
                JSONB json = JSONB.valueOf(((JsonArray) value).encode());
                return converterCast.from(json);
            }
        }
        if (value instanceof JsonObject) {
            if (converter != null && converter.fromType().equals(JSON.class)) {
                Converter<JSON, T> converterCast = (Converter<JSON, T>) converter;
                JSON json = JSON.valueOf(((JsonObject) value).encode());
                return converterCast.from(json);
            } else if (converter != null && converter.fromType().equals(JSONB.class)) {
                Converter<JSONB, T> converterCast = (Converter<JSONB, T>) converter;
                JSONB json = JSONB.valueOf(((JsonObject) value).encode());
                return converterCast.from(json);
            }
        }
        if (value instanceof Numeric) {
            Numeric numericValue = (Numeric) value;
            if (converter.fromType().equals(BigDecimal.class)) {
                return ((Converter<BigDecimal, T>) converter).from(numericValue.bigDecimalValue());
            }
            if (converter.fromType().equals(BigInteger.class)) {
                return ((Converter<BigInteger, T>) converter).from(numericValue.bigIntegerValue());
            }
            if (converter.fromType().equals(Long.class)) {
                return ((Converter<Long, T>) converter).from(numericValue.longValue());
            }
            if (converter.fromType().equals(Integer.class)) {
                return ((Converter<Integer, T>) converter).from(numericValue.intValue());
            }
            if (converter.fromType().equals(Integer.class)) {
                return ((Converter<Integer, T>) converter).from(numericValue.intValue());
            }
            if (converter.fromType().equals(Double.class)) {
                return ((Converter<Double, T>) converter).from(numericValue.doubleValue());
            }
            if (converter.fromType().equals(Float.class)) {
                return ((Converter<Float, T>) converter).from(numericValue.floatValue());
            }
            if (converter.fromType().equals(Short.class)) {
                return ((Converter<Short, T>) converter).from(numericValue.shortValue());
            }
        }
        if (value instanceof LocalDateTime && converter.fromType().equals(Timestamp.class)) {
            return ((Converter<Timestamp, T>) converter).from(Timestamp.valueOf((LocalDateTime) value));
        }

        // Quand on lit un champ json avec la valeur null (la valeur JSON nulle, pas celle de la BDD)
        // la valeur lue est juste un Object (pas une sous classe)
        if(value.getClass().equals(Object.class)) {
            return (T)JSON.valueOf("null");
        }

        return ((Converter<Object, T>) converter).from(value);
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
