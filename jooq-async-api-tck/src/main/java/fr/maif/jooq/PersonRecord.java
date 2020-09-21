package fr.maif.jooq;

import org.jooq.*;
import org.jooq.impl.TableRecordImpl;

import java.sql.Time;
import java.sql.Timestamp;

public class PersonRecord extends TableRecordImpl<PersonRecord> implements Record3<String, JSON, Timestamp> {

    public PersonRecord(Table<PersonRecord> table) {
        super(table);
    }

    public PersonRecord() {
        super(Person.PERSON);
    }

    @Override
    public Field<String> field1() {
        return Person.PERSON.NOM;
    }

    @Override
    public Field<JSON> field2() {
        return Person.PERSON.METADATA;
    }

    @Override
    public Field<Timestamp> field3() {
        return Person.PERSON.CREATED;
    }

    @Override
    public String value1() {
        return (String)get(0);
    }

    @Override
    public JSON value2() {
        return (JSON)get(1);
    }

    @Override
    public Timestamp value3() {
        return (Timestamp)get(2);
    }

    @Override
    public PersonRecord value1(String value) {
        set(0, value);
        return this;
    }

    @Override
    public PersonRecord value2(JSON value) {
        set(1, value);
        return this;
    }

    @Override
    public Record3<String, JSON, Timestamp> value3(Timestamp value) {
        set(2, value);
        return this;
    }

    @Override
    public PersonRecord values(String s, JSON json, Timestamp timestamp) {
        value1(s);
        value2(json);
        value3(timestamp);
        return this;
    }

    @Override
    public String component1() {
        return value1();
    }

    @Override
    public JSON component2() {
        return value2();
    }

    @Override
    public Timestamp component3() {
        return value3();
    }

    public Row3<String, JSON, Timestamp> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<String, JSON, Timestamp> valuesRow() {
        return (Row3) super.valuesRow();
    }

}
