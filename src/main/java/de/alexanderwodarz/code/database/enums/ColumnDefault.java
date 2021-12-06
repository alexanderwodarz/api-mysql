package de.alexanderwodarz.code.database.enums;

public enum ColumnDefault {

    CURRENT_TIMESTAMP("CURRENT_TIMESTAMP"),
    UNIX_TIMESTAMP("UNIX_TIMESTAMP()"),
    INTEGER(""),
    empty("");

    private String method;

    ColumnDefault(String method){
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
