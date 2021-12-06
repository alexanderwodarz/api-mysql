package de.alexanderwodarz.code.database.enums;

public enum ColumnType {

    TIMESTAMP("timestamp"),
    empty("empty");

    private String name;

    ColumnType(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
