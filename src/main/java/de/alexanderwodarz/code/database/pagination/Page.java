package de.alexanderwodarz.code.database.pagination;

import de.alexanderwodarz.code.database.AbstractTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Page<T extends AbstractTable> {

    private final Pagination<T> pagination;
    private final int page;

    public List<T> getContent() {
        List<T> content = new ArrayList<>();
        String query = pagination.getSelector().buildQuery() + " LIMIT " + (page * pagination.getLimit()) + ", " + pagination.getLimit();
        try {
            ResultSet rs = pagination.getTable().getDatabase().query(query);
            if (rs.next()) {
                while (!rs.isAfterLast()) {
                    content.add((T) pagination.getTable().setValues(rs));
                    rs.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return content;
    }

}
