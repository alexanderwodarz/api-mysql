package de.alexanderwodarz.code.database.query;

import de.alexanderwodarz.code.database.AbstractTable;
import de.alexanderwodarz.code.database.Database;
import de.alexanderwodarz.code.database.annotation.Column;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class QuerySelector<T extends AbstractTable> {

    private final T t;
    private final boolean verbose;
    private List<QueryParameter> parameters = new ArrayList<>();

    private int limit;

    public QuerySelector addParameter(QueryParameter parameter) {
        this.parameters.add(parameter);
        return this;
    }

    public QueryParameter addParameter() {
        return new QueryParameter(this);
    }

    public List<T> executeMany() {
        List<T> list = new ArrayList<>();
        String query = buildQuery();
        if (verbose)
            System.out.println(query);
        try {
            ResultSet rs = t.getDatabase().query(query);
            if (rs.next()) {
                while (!rs.isAfterLast()) {
                    T entry = (T) t.getClass().getConstructor(Database.class).newInstance(t.getDatabase());
                    for (Field field : entry.getClass().getFields()) {
                        if (!field.isAnnotationPresent(Column.class))
                            continue;
                        Column column = field.getAnnotation(Column.class);
                        setFieldValue(field, entry, rs.getObject(column.name().length() == 0 ? field.getName() : column.name()));
                    }
                    list.add(entry);
                    rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public T executeOne() {
        List<T> list = executeMany();
        return list.size() == 0 ? null : list.get(0);
    }

    public String buildQuery() {
        String query = "SELECT * FROM " + t.getName();

        if (parameters.size() > 0)
            query += " WHERE";
        for (QueryParameter parameter : parameters)
            query += " " + parameter + " AND";
        if (parameters.size() > 0)
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
