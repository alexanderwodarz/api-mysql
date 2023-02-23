package de.alexanderwodarz.code.database.update;

import de.alexanderwodarz.code.database.AbstractTable;
import de.alexanderwodarz.code.database.Selector;
import de.alexanderwodarz.code.database.query.Parameter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateSelector<T extends AbstractTable> extends Selector<T> {

    private final T t;

    public T execute() {
        System.out.println(buildQuery());
        return null;
    }

    public String buildQuery() {
        String update = "UPDATE " + t.getName() + " SET ";
        for (Parameter parameter : getParameters()) {
            update += "`"+parameter.getKey()+ "`='"+parameter.getValue()+"'";
        }
        return update;
    }

}
