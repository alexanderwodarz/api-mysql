package de.alexanderwodarz.code.database.query;

import de.alexanderwodarz.code.database.AbstractTable;
import de.alexanderwodarz.code.database.Database;
import de.alexanderwodarz.code.database.Selector;
import de.alexanderwodarz.code.database.annotation.Column;
import de.alexanderwodarz.code.database.enums.QueryOperator;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QuerySelector<T extends AbstractTable> extends Selector<T> {

    private final T t;
    private final boolean verbose;
    public Parameter addParameter() {
        return new Parameter(this);
    }

    public QuerySelector<T> addParameter(Parameter parameter) {
        parameters.add(parameter);
        return this;
    }

    public QuerySelector<T> addParameter(String key, Object value, QueryOperator operator) {
        return (QuerySelector<T>) new Parameter(this).setKey(key).setValue(value).setOperator(operator).build();
    }

    public QuerySelector<T> addParameter(String key, Object value) {
        return addParameter(key, value, QueryOperator.EQUALS);
    }

    private int limit;

    public List<T> executeMany() {
        List<T> list = new ArrayList<>();
        String query = buildQuery();
        if (verbose)
            System.out.println(query);
        try {
            ResultSet rs = t.getDatabase().query(query);
            if (rs.next()) {
                while (!rs.isAfterLast()) {
                    list.add(setVariables(rs));
                    rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public T setVariables(ResultSet rs) {
        try {
            T entry = (T) t.getClass().getConstructor(Database.class).newInstance(t.getDatabase());
            for (Field field : entry.getClass().getFields()) {
                if (!field.isAnnotationPresent(Column.class))
                    continue;
                Column column = field.getAnnotation(Column.class);
                setFieldValue(field, entry, rs.getObject(column.name().length() == 0 ? field.getName() : column.name()));
            }
            return entry;
        } catch (Exception e) {
            return null;
        }
    }

    public T executeOne() {
        List<T> list = executeMany();
        return list.size() == 0 ? null : list.get(0);
    }

    public String buildQuery() {
        String query = "SELECT * FROM " + t.getName();

        if (getParameters().size() > 0)
            query += " WHERE";
        for (Object parameter : getParameters())
            query += " " + parameter + " AND";
        if (getParameters().size() > 0)
            query = query.substring(0, query.length() - 4);
        if (limit > 0)
            query += " LIMIT " + limit;
        return query;
    }

    public int getLimit() {
        return limit;
    }

    public QuerySelector setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public int getRowCount() {
        try {
            ResultSet rs = t.getDatabase().query("SELECT COUNT(*) FROM " + t.getName() + ";");
            if (rs.next()) {
                return rs.getInt("COUNT(*)");
            }
        } catch (SQLException e) {

        }
        return 0;
    }


    public void setFieldValue(Field field, Object t, Object set) throws IllegalAccessException {
        if (field.getType() == String.class)
            field.set(t, set == null ? "" : set.toString());
        if (field.getType() == int.class) {
            try {
                field.set(t, set == null ? 0 : set);
            } catch (Exception e) {
                field.set(t, Boolean.parseBoolean(set + "") ? 1 : 0);
            }
        }
        if (field.getType() == boolean.class)
            field.set(t, Boolean.parseBoolean(set.toString()));
        if (field.getType() == long.class) {
            if (set == null)
                set = 0;
            field.set(t, Long.parseLong(set.toString()));
        }
    }
}
