package fr.maif.jooq.reactive;

import fr.maif.jooq.QueryResult;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.data.*;
import io.vertx.pgclient.data.Path;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.impl.DefaultConverterProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;


public class ReactiveRowQueryResult implements QueryResult {

    private final Row current;

    private final ConverterProvider converterProvider = new DefaultConverterProvider();

    public ReactiveRowQueryResult(Row row) {
        this.current = row;
    }

    @Override
    public <T> T get(Field<T> field) {
        if (isSupportedClass(field.getConverter().fromType())) {
            Object value = current.get(field.getConverter().fromType(), field.getName());
            return handleValue(value, field.getConverter());
        } else {
            Object value = current.getValue(field.getName());
            return handleValue(value, field.getConverter());
        }
    }

    @Override
    public <T> T get(int index, Class<T> type) {
        Object value = current.getValue(index);
        Converter<Object, T> provide = converterProvider.provide(Object.class, type);
        if (Objects.isNull(provide)) {
            throw new IllegalStateException("Provider not found");
        }
        return provide.from(value);
    }

    @Override
    public <T> T get(int index, Field<T> field) {
        return handleValue(current.getValue(index), field.getConverter());
    }

    @Override
    public <T> T get(String columnName, Class<T> type) {
        Object value = current.getValue(columnName);
        Converter<Object, T> provide = converterProvider.provide(Object.class, type);
        if (Objects.isNull(provide)) {
            throw new IllegalStateException("Provider not found");
        }
        return provide.from(value);
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

        if (value instanceof String && converter.fromType().equals(JSON.class)) {
            Converter<JSON, T> converterCast = (Converter<JSON, T>) converter;
            return converterCast.from(JSON.json( (String) value));
        }
        if (value instanceof String && converter.fromType().equals(JSONB.class)) {
            Converter<JSONB, T> converterCast = (Converter<JSONB, T>) converter;
            return converterCast.from(JSONB.jsonb( (String) value));
        }

        // Quand on lit un champ json avec la valeur null (la valeur JSON nulle, pas celle de la BDD)
        // la valeur lue est juste un Object (pas une sous classe)
        if(value.getClass().equals(Object.class)) {
            return (T)JSON.valueOf("null");
        }

        return ((Converter<Object, T>) converter).from(value);
    }

    public <T> boolean isSupportedClass(Class<T> type) {
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            if (componentType == Boolean.class) {
                return true;
            } else if (componentType == Short.class) {
                return true;
            } else if (componentType == Integer.class) {
                return true;
            } else if (componentType == Long.class) {
                return true;
            } else if (componentType == Float.class) {
                return true;
            } else if (componentType == Double.class) {
                return true;
            } else if (componentType == String.class) {
                return true;
            } else if (componentType == Buffer.class) {
                return true;
            } else if (componentType == UUID.class) {
                return true;
            } else if (componentType == LocalDate.class) {
                return true;
            } else if (componentType == LocalTime.class) {
                return true;
            } else if (componentType == OffsetTime.class) {
                return true;
            } else if (componentType == LocalDateTime.class) {
                return true;
            } else if (componentType == OffsetDateTime.class) {
                return true;
            } else if (componentType == Interval.class) {
                return true;
            } else if (componentType == Numeric.class) {
                return true;
            } else if (componentType == Point.class) {
                return true;
            } else if (componentType == Line.class) {
                return true;
            } else if (componentType == LineSegment.class) {
                return true;
            } else if (componentType == Path.class) {
                return true;
            } else if (componentType == Polygon.class) {
                return true;
            } else if (componentType == Circle.class) {
                return true;
            } else if (componentType == Interval.class) {
                return true;
            } else if (componentType == Box.class) {
                return true;
            } else if (componentType == Object.class) {
                return true;
            } else if (componentType.isEnum()) {
                return true;
            }
        } else {
            if (type == Boolean.class) {
                return true;
            } else if (type == Short.class) {
                return true;
            } else if (type == Integer.class) {
                return true;
            } else if (type == Long.class) {
                return true;
            } else if (type == Float.class) {
                return true;
            } else if (type == Double.class) {
                return true;
            } else if (type == Numeric.class) {
                return true;
            } else if (type == String.class) {
                return true;
            } else if (type == Buffer.class) {
                return true;
            } else if (type == UUID.class) {
                return true;
            } else if (type == LocalDate.class) {
                return true;
            } else if (type == LocalTime.class) {
                return true;
            } else if (type == OffsetTime.class) {
                return true;
            } else if (type == LocalDateTime.class) {
                return true;
            } else if (type == OffsetDateTime.class) {
                return true;
            } else if (type == Interval.class) {
                return true;
            } else if (type == Point.class) {
                return true;
            } else if (type == Line.class) {
                return true;
            } else if (type == LineSegment.class) {
                return true;
            } else if (type == Path.class) {
                return true;
            } else if (type == Polygon.class) {
                return true;
            } else if (type == Circle.class) {
                return true;
            } else if (type == Box.class) {
                return true;
            } else if (type == JsonObject.class) {
                return true;
            } else if (type == JsonArray.class) {
                return true;
            } else if (type == Object.class) {
                return true;
            } else if (type.isEnum()) {
                return true;
            }
        }
        return false;
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
