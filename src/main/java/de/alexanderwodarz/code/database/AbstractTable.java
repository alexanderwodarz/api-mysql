package de.alexanderwodarz.code.database;

import de.alexanderwodarz.code.database.annotation.Column;
import de.alexanderwodarz.code.database.annotation.Table;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTable {

    private Database database;

    public AbstractTable(Database database) {
        this.database = database;
    }

    public void check() {

    }

    @SneakyThrows
    public <T> List<T> getAll(T filter, boolean verbose) {
        List<T> list = new ArrayList<>();
        String query = generateQuery(filter.getClass().getFields(), filter);
        if (verbose)
            System.out.println(query);
        try {
            ResultSet rs = database.query(query);
            if (rs.next()) {
                while (!rs.isAfterLast()) {
                    T entry = (T) filter.getClass().getConstructor(Database.class).newInstance(database);
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
        }
        return list;
    }

    @SneakyThrows
    public <T> List<T> getAll(T filter) {
        return getAll(filter, false);
    }

    private <T> String generateQuery(Field[] fields, T t) {
        if (!(t instanceof AbstractTable))
            return "";
        AbstractTable table = (AbstractTable) t;
        String query = "SELECT * FROM " + table.getName();
        String wheres = "";
        for (Field field : fields) {
            try {
                if (field.get(table) instanceof Integer) {
                    if (field.getInt(table) > 0) {
                        wheres += "`" + field.getName() + "`='" + field.get(table) + "' AND ";
                    }
                }
                if (field.get(table) instanceof String) {
                    if (field.get(table).toString() != null) {
                        Column column = field.getAnnotation(Column.class);
                        wheres += "`" + (column.name().length() == 0 ? field.getName() : column.name()) + "`='" + field.get(table) + "' AND ";
                    }
                }
                if (field.get(table) instanceof Long) {
                    if (field.getLong(table) > 0) {
                        wheres += "`" + field.getName() + "`='" + field.get(table) + "' AND ";
                    }
                }
                if (field.get(table) instanceof Boolean) {
                    wheres += "`" + field.getName() + "`='" + (field.getBoolean(table) ? "1" : "0") + "' AND ";
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (wheres.length() > 0)
            query += " WHERE " + wheres.substring(0, wheres.length() - 5);
        return query;
    }

    public void create() {
        create(false);
    }

    public void create(boolean verbose) {
        String name = getName();
        String query = "CREATE TABLE IF NOT EXISTS " + name + " (";
        for (Field field : this.getClass().getFields()) {
            query += getFieldCreation(field);
        }
        query = query.substring(0, query.length() - 1);
        if (database.getType().equals("mysql")) {
            if (hasPrimaryKey()) {
                query += ", primary key (";
                for (Field field : this.getClass().getFields()) {
                    if (!field.isAnnotationPresent(Column.class))
                        continue;
                    if (!field.getAnnotation(Column.class).primaryKey())
                        continue;
                    query += getColumnNameFromField(field) + ",";
                }
                query = query.substring(0, query.length() - 1) + ")";
            }
        }
        if (hasForeignKey()) {
            for (Field field : this.getClass().getFields()) {
                if (!field.isAnnotationPresent(Column.class))
                    continue;
                if (field.getAnnotation(Column.class).foreignKey().length() == 0)
                    continue;
                query += getColumnForeignKey(field);
            }
        }
        query += ")";
        if (verbose)
            System.out.println(query);
        database.update(query, null);
    }

    public String getColumnForeignKey(Field field) {
        Column column = field.getAnnotation(Column.class);
        return ", constraint " + getName() + "_" + column.foreignKeyTable() + "_" + getColumnNameFromField(field) + "_fk foreign key (" + getColumnNameFromField(field) + ") references " + column.foreignKeyTable() + " (" + column.foreignKey() + ") on update " + column.foreignKeyUpdate() + " on delete " + column.foreignKeyDelete();
    }

    public boolean hasPrimaryKey() {
        for (Field field : this.getClass().getFields()) {
            if (!field.isAnnotationPresent(Column.class))
                continue;
            if (field.getAnnotation(Column.class).primaryKey())
                return true;
        }
        return false;
    }

    public boolean hasForeignKey() {
        for (Field field : this.getClass().getFields()) {
            if (!field.isAnnotationPresent(Column.class))
                continue;
            if (field.getAnnotation(Column.class).foreignKey().length() > 0)
                return true;
        }
        return false;
    }

    public <T> T getOne(T t) {
        try {
            return getAll(t).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public <T> T getOne(T t, boolean verbose) {
        try {
            return getAll(t, verbose).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public void delete() {
        delete(false);
    }

    @SneakyThrows
    public void delete(boolean verbose) {
        String delete = "DELETE FROM " + getName() + " ";
        String wheres = "";
        for (Field field : getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Column.class))
                continue;
            if (field.getAnnotation(Column.class).autoIncrement())
                continue;
            if (field.get(this) == null)
                continue;
            wheres += "`" + field.getName() + "`='" + field.get(this) + "' AND ";
        }
        if (wheres.length() > 0)
            delete += "WHERE " + wheres.substring(0, wheres.length() - 4);
        delete += ";";
        if (verbose)
            System.out.println(delete);
        database.update(delete, new ArrayList<>());
    }

    public void insert() {
        insert(false);
    }

    public void insert(boolean verbose) {
        String name = getName();
        String insert = "INSERT INTO " + name + " ";
        String names = "";
        String value = "";
        List<Object> values = new ArrayList<>();
        for (Field field : getClass().getFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                if (!field.getAnnotation(Column.class).autoIncrement()) {
                    try {
                        if (field.get(this) == null)
                            continue;
                        names += "`" + field.getName() + "`, ";
                        value += "?,";
                        values.add(field.get(this));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        names = names.substring(0, names.length() - 2);
        insert += "(" + names + ") VALUES (" + value.substring(0, value.length() - 1) + ");";
        if (verbose)
            System.out.println(insert);
        database.update(insert, values);
    }

    public String getFieldCreation(Field field) {
        if (!field.isAnnotationPresent(Column.class))
            return "";
        Column col = field.getAnnotation(Column.class);
        String creation = "`" + getColumnNameFromField(field) + "`";
        if (database.getType().equals("mysql")) {
            creation += " " + DataType.getByName(field.getType().getSimpleName()).getName() + (col.length() == 0 ? "" : "(" + col.length() + ")");
            creation += " " + (col.autoIncrement() ? "auto_increment" : "null");
            creation += ",";
        } else {
            creation += " " + DataType.getByName(field.getType().getSimpleName()).getName() + (col.length() == 0 ? "" : "(" + col.length() + ")");
            creation += (col.primaryKey() || col.autoIncrement() ? " constraint " + getName() + " primary key" + (col.autoIncrement() ? " autoincrement" : "") : "");
            creation += ",";
        }
        return creation;
    }

    public String getColumnNameFromField(Field f) {
        if (!f.isAnnotationPresent(Column.class))
            return "";
        if (f.getAnnotation(Column.class).name().length() == 0)
            return f.getName();
        else
            return f.getAnnotation(Column.class).name();
    }

    public String getName() {
        Table ann = this.getClass().getAnnotation(Table.class);
        String name = "";
        if (ann.name().length() == 0)
            name = this.getClass().getSimpleName().toLowerCase();
        else
            name = ann.name();
        return name;
    }

    private void setFieldValue(Field field, Object t, Object set) throws IllegalAccessException {
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
        if (field.getType() == long.class)
            field.set(t, Long.parseLong(set.toString()));
    }
}
