package de.alexanderwodarz.code.database.enums;

public enum DataType {

    varchar("String", "varchar"),
    Boolean("boolean","boolean"),
    INTEGER("int", "INTEGER"),
    tinyint("boolean", "tinyint"),
    datetime("String", "datetime"),
    numeric("long", "numeric");

    private String type, name;

    private DataType(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static DataType getByName(String name) {
        for (DataType value : DataType.values())
            if (value.getType().equals(name))
                return value;
        return null;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

}
