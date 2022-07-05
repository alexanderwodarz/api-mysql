package de.alexanderwodarz.code.database.enums;

public enum QueryOperator {

    EQUALS("="),
    LOWER("<"),
    HIGHER(">");

    private final String operator;

    QueryOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        return getOperator();
    }
}
