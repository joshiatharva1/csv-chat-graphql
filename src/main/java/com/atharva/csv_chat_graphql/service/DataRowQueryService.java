package com.nupur.csv_chat_graphql.service;

import com.nupur.csv_chat_graphql.DataCell;
import com.nupur.csv_chat_graphql.DataRow;
import com.nupur.csv_chat_graphql.QDataRow;
import com.nupur.csv_chat_graphql.QDataCell;
import com.nupur.csv_chat_graphql.repository.DataRowRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DataRowQueryService {

    private final DataRowRepository repository;

    // QueryDSL factory (already configured via QuerydslConfig)
    @Autowired
    private JPAQueryFactory queryFactory;

    public DataRowQueryService(DataRowRepository repository) {
        this.repository = repository;
    }

    public List<DataRow> findAllRows() {
        return repository.findAll();
    }

    /**
     * Basic equality filter: where columnName = value (case-insensitive).
     * Current implementation: in-memory filtering over repository.findAll().
     */
    public List<DataRow> findRowsByColumnValue(String columnName, String value) {
        String col = columnName.toLowerCase(Locale.ROOT);
        String val = value.toLowerCase(Locale.ROOT);

        return repository.findAll()
                .stream()
                .filter(row -> row.getCells() != null &&
                        row.getCells().stream().anyMatch(cell ->
                                cell.getColumnName() != null &&
                                        cell.getCellValue() != null &&
                                        cell.getColumnName().equalsIgnoreCase(col) &&
                                        cell.getCellValue().equalsIgnoreCase(val)))
                .collect(Collectors.toList());
    }

    /**
     * Same "columnName = value" filter implemented using QueryDSL.
     * Generates a JOIN + WHERE at SQL level.
     */
    public List<DataRow> findRowsByColumnValueUsingQuerydsl(String columnName, String value) {
        if (columnName == null || value == null) {
            return List.of();
        }

        String col = columnName.trim();
        String val = value.trim();
        if (col.isEmpty() || val.isEmpty()) {
            return List.of();
        }

        QDataRow row = QDataRow.dataRow;
        QDataCell cell = QDataCell.dataCell;

        return queryFactory
                .selectDistinct(row)
                .from(row)
                .join(row.cells, cell)
                .where(
                        cell.columnName.equalsIgnoreCase(col)
                                .and(cell.cellValue.equalsIgnoreCase(val))
                )
                .fetch();
    }

    /**
     * AND query: (col1 = val1) AND (col2 = val2).
     * Still using in-memory filtering for now.
     */
    public List<DataRow> findRowsByTwoColumns(String column1, String value1,
                                              String column2, String value2) {

        String col1 = column1.toLowerCase(Locale.ROOT);
        String val1 = value1.toLowerCase(Locale.ROOT);
        String col2 = column2.toLowerCase(Locale.ROOT);
        String val2 = value2.toLowerCase(Locale.ROOT);

        return repository.findAll()
                .stream()
                .filter(row -> matches(row, col1, val1) && matches(row, col2, val2))
                .collect(Collectors.toList());
    }

    private boolean matches(DataRow row, String col, String val) {
        if (row.getCells() == null) return false;

        return row.getCells().stream().anyMatch(cell ->
                cell.getColumnName() != null &&
                        cell.getCellValue() != null &&
                        cell.getColumnName().equalsIgnoreCase(col) &&
                        cell.getCellValue().equalsIgnoreCase(val)
        );
    }

    /**
     * Filter by column = value, then sort by another column, then limit.
     * Used for: "show top N rows where ... order by ... asc/desc"
     */
    public List<DataRow> findRowsByColumnValueSortedLimited(
            String filterColumn,
            String filterValue,
            String orderByColumn,
            boolean descending,
            int limit
    ) {
        String filterCol = filterColumn.toLowerCase(Locale.ROOT);
        String filterVal = filterValue.toLowerCase(Locale.ROOT);
        String orderCol = orderByColumn.toLowerCase(Locale.ROOT);

        @SuppressWarnings("unchecked")
        Comparator<DataRow> comparator =
                Comparator.comparing(row -> (Comparable) getComparableValue(row, orderCol));

        if (descending) {
            comparator = comparator.reversed();
        }

        return repository.findAll()
                .stream()
                .filter(row -> matches(row, filterCol, filterVal))
                .sorted(comparator)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Comparable<?> getComparableValue(DataRow row, String column) {
        if (row.getCells() == null) return "";

        return row.getCells().stream()
                .filter(c -> c.getColumnName() != null &&
                        c.getColumnName().equalsIgnoreCase(column))
                .map(DataCell::getCellValue)
                .findFirst()
                .map(val -> {
                    // try numeric comparison first
                    try {
                        return Double.parseDouble(val);
                    } catch (NumberFormatException e) {
                        return val; // fallback to string compare
                    }
                })
                .orElse("");
    }

    // --------------------------------------------------------------------
    // 🔹 QueryDSL-style wrapper methods used by NaturalLanguageQueryService
    //    For now they just delegate to the existing in-memory methods,
    //    so behavior stays exactly the same and everything compiles.
    // --------------------------------------------------------------------

    public List<DataRow> findRowsByTwoColumnsUsingQuerydsl(
            String column1,
            String value1,
            String column2,
            String value2
    ) {
        return findRowsByTwoColumns(column1, value1, column2, value2);
    }

    public List<DataRow> findRowsByColumnValueSortedLimitedUsingQuerydsl(
            String filterColumn,
            String filterValue,
            String orderByColumn,
            boolean descending,
            int limit
    ) {
        return findRowsByColumnValueSortedLimited(
                filterColumn,
                filterValue,
                orderByColumn,
                descending,
                limit
        );
    }
}