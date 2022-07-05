package de.alexanderwodarz.code.database.query;

import de.alexanderwodarz.code.database.enums.QueryOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryParameter {

    private final QuerySelector selector;
    @Getter
    private String key, value, operator = "=";

    public QueryParameter setKey(String key) {
        this.key = key;
        return this;
    }

    public QueryParameter setValue(Object value) {
        this.value = value.toString();
        return this;
    }

    public QueryParameter setOperator(QueryOperator operator) {
        this.operator = operator.toString();
        return this;
    }

    public QuerySelector build() {
        return selector.addParameter(this);
    }

    @Override
    public String toString() {
        return "`" + getKey() + "` " + getOperator() + " '" + getValue() + "'";
    }
}
