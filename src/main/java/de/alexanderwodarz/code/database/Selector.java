package de.alexanderwodarz.code.database;

import de.alexanderwodarz.code.database.enums.QueryOperator;
import de.alexanderwodarz.code.database.query.Parameter;

import java.util.ArrayList;
import java.util.List;

public class Selector<T extends AbstractTable> {

    public List<Parameter> parameters = new ArrayList<>();

    public Parameter addParameter() {
        return new Parameter(this);
    }

    public Selector<T> addParameter(Parameter parameter) {
        parameters.add(parameter);
        return this;
    }

    public Selector<T> addParameter(String key, Object value, QueryOperator operator) {
        return new Parameter(this).setKey(key).setValue(value).setOperator(operator).build();
    }

    public Selector<T> addParameter(String key, Object value) {
        return addParameter(key, value, QueryOperator.EQUALS);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}
