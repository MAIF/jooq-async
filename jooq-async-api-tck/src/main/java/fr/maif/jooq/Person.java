package fr.maif.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.sql.Time;
import java.sql.Timestamp;

public class Person extends TableImpl<PersonRecord> {
    public static final Person PERSON = new Person();

    @Override
    public Class<PersonRecord> getRecordType() {
        return PersonRecord.class;
    }

    public final TableField<PersonRecord, String> NOM = createField(DSL.name("nom"),
            org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    public final TableField<PersonRecord, JSON> METADATA = createField(DSL.name("metadata"),
            SQLDataType.JSON.nullable(false), this, "");

    public final TableField<PersonRecord, Timestamp> CREATED = createField(DSL.name("created"),
            SQLDataType.TIMESTAMP.nullable(true), this, "");

    public Person() {
        this(DSL.name("person"), null);
    }

    public Person(String alias) {
        this(DSL.name(alias), PERSON);
    }

    public Person(Name alias) {
        this(alias, PERSON);
    }

    private Person(Name alias, Table<PersonRecord> aliased) {
        this(alias, aliased, null);
    }

    private Person(Name alias, Table<PersonRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""));
    }

    public <O extends Record> Person(Table<O> child, ForeignKey<O, PersonRecord> key) {
        super(child, key, PERSON);
    }

    @Override
    public Person as(String alias) {
        return new Person(DSL.name(alias), this);
    }

    @Override
    public Person as(Name alias) {
        return new Person(alias, this);
    }

    @Override
    public Person rename(String name) {
        return new Person(DSL.name(name), null);
    }

    @Override
    public Person rename(Name name) {
        return new Person(name, null);
    }

    // -------------------------------------------------------------------------
    // Row7 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<String, JSON, Timestamp> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

}