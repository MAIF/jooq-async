package fr.maif.jooq;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.function.Supplier;


/**
 * A query result wrapper for those returned by the different drivers. It provides some methods to access the actual result
 * type. If the query was supposed to return more than one entry, call {@code QueryResult#asList()} once, which returns
 * a list with {@code QueryResults} for each row returned.
 */
public interface QueryResult {

    /**
     * Returns a value for a {@code Field}.
     * @param field the {@code Field} to get.
     * @param <T>
     * @return The field's value or {@code null}.
     */
    <T> T get(Field<T> field);

    /**
     * Returns a value by index. Because jOOQ-{@code Converters} are not respected by all implementations favor
     * {@code QueryResult#get(Field<T>)} method.
     * @param index the index of the column's value you wish to get.
     * @param type the expected type of that column.
     * @param <T>
     * @return The field's value or {@code null}.
     * @throws ClassCastException If the column is mapped by a jOOQ-{@code Converter}, the underlying implementation
     * might throw a {@code ClassCastException} because the non-jOOQ drivers are not aware of converters. For correct
     * handling for fields with converters favor {@code QueryResult#get(Field<T>)} method.
     */
    <T> T get(int index, Class<T> type);


    /**
     * Returns a value by index. Because jOOQ-{@code Converters} are not respected by all implementations favor
     * @param index the index of the column's value you wish to get.
     * @param field the expected type of that column.
     * @param <T>
     * @return
     */
    <T> T get(int index, Field<T> field);

    /**
     * Returns a value by name. Because jOOQ-{@code Converters} are not respected by all implementations favor
     * {@code QueryResult#get(Field<T>)} method.
     * @param columnName the name of the column you wish to get.
     * @param type the expected type of that column.
     * @param <T>
     * @return The field's value or {@code null}.
     * @throws ClassCastException If the column is mapped by a jOOQ-{@code Converter}, the underlying implementation
     * might throw a {@code ClassCastException} because the non-jOOQ drivers are not aware of converters. For correct
     * handling for fields with converters favor {@code QueryResult#get(Field<T>)} method.
     */
    <T> T get(String columnName, Class<T> type);

    <T extends Record> T toRecord(Table<T> table);
    <T extends Record> T toRecord(T record);

}
