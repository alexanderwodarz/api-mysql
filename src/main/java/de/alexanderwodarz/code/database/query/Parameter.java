package de.alexanderwodarz.code.database.query;

import de.alexanderwodarz.code.database.Selector;
import de.alexanderwodarz.code.database.enums.QueryOperator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Parameter {

    private final Selector selector;
    @Getter
    private String key, value, operator = "=";

    public Parameter setKey(String key) {
        this.key = key;
        return this;
    }

    public Parameter setValue(Object value) {
        this.value = value.toString();
        return this;
    }

    public Parameter setOperator(QueryOperator operator) {
        this.operator = operator.toString();
        return this;
    }

    public Selector build() {
        return selector.addParameter(this);
    }

    @Override
    public String toString() {
        return "`" + getKey() + "` " + getOperator() + " '" + getValue() + "'";
    }
}
