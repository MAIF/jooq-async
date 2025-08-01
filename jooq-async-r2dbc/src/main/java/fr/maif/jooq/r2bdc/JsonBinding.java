package fr.maif.jooq.r2bdc;

import io.r2dbc.postgresql.codec.Json;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;

class JsonBinding implements Binding<JSON, Json> {

    @Override
    public Converter<JSON, Json> converter() {
        return new Converter<JSON, Json>() {
            @Override
            public Json from(JSON databaseObject) {
                return Json.of(databaseObject.data());
            }

            @Override
            public JSON to(Json userObject) {
                return JSON.jsonOrNull(userObject.asString());
            }

            @Override
            public Class<JSON> fromType() {
                return JSON.class;
            }

            @Override
            public Class<Json> toType() {
                return Json.class;
            }
        };
    }

    @Override
    public void sql(BindingSQLContext<Json> ctx) throws SQLException {
        if (ctx.render().paramType() == ParamType.INLINED)
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::json");
        else
            ctx.render().sql(ctx.variable()).sql("::json");
    }

    @Override
    public void register(BindingRegisterContext<Json> ctx) throws SQLException {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    @Override
    public void set(BindingSetStatementContext<Json> ctx) throws SQLException {
        JSON json = ctx.convert(converter()).value();
        ctx.statement().setString(ctx.index(), json == null ? null : json.data());
    }

    @Override
    public void set(BindingSetSQLOutputContext<Json> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void get(BindingGetResultSetContext<Json> ctx) throws SQLException {
        ctx.convert(converter()).value(JSON.json(ctx.resultSet().getString(ctx.index())));
    }

    @Override
    public void get(BindingGetStatementContext<Json> ctx) throws SQLException {
        ctx.convert(converter()).value(JSON.json(ctx.statement().getString(ctx.index())));
    }

    @Override
    public void get(BindingGetSQLInputContext<Json> ctx) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
