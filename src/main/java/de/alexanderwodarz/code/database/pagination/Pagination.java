package de.alexanderwodarz.code.database.pagination;

import de.alexanderwodarz.code.database.AbstractTable;
import de.alexanderwodarz.code.database.query.QuerySelector;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Pagination<T extends AbstractTable> {

    private T table;
    private int limit;
    private QuerySelector selector;

    public Pagination(T table, int limit, QuerySelector selector) {
        this.table = table;
        this.limit = limit;
        this.selector = selector;
    }

    public int getElementCount() {
        try {
            ResultSet rs = table.getDatabase().query(selector.buildQuery().replaceAll("\\*", "COUNT(*)"));
            if (rs.next()) {
                return rs.getInt("COUNT(*)");
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    public Page<T> getPage(int page) {
        return new Page<>(this, page);
    }

    public int getPageCount() {
        int max = getElementCount();
        int pageCount = max / limit;
        if (max % limit > 0)
            pageCount++;
        return pageCount;
    }

    public List<Page<T>> getPages() {
        List<Page<T>> pages = new ArrayList<>();
        for (int i = 0; i < getPageCount(); i++)
            pages.add(getPage(i));
        return pages;
    }

}
