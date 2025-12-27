package com.nupur.csv_chat_graphql.service;

import com.nupur.csv_chat_graphql.DataCell;
import com.nupur.csv_chat_graphql.DataRow;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NaturalLanguageQueryService {

    private final DataRowQueryService dataRowQueryService;

    public NaturalLanguageQueryService(DataRowQueryService dataRowQueryService) {
        this.dataRowQueryService = dataRowQueryService;
    }

    /**
     * Used by GraphQL.
     */
    public List<DataRow> ask(String question) {
        return query(question);
    }

    /**
     * Main implementation for natural language query.
     *
     * Now:
     *  - For common patterns we use QueryDSL via DataRowQueryService
     *  - For anything more complex we fall back to the old in-memory logic
     */
    public List<DataRow> query(String question) {
        // Load all rows for the fallback in-memory path
        List<DataRow> allRows = dataRowQueryService.findAllRows();
        if (allRows.isEmpty()) {
            return allRows;
        }

        String original = question == null ? "" : question.trim();
        String lower = original.toLowerCase(Locale.ROOT);

        // --- 1) WHERE part parsing ---
        int whereIdx = lower.indexOf("where");
        int orderIdx = lower.indexOf("order by");

        String wherePart = null;
        if (whereIdx != -1) {
            int start = whereIdx + "where".length();
            int end = (orderIdx != -1) ? orderIdx : lower.length();
            wherePart = lower.substring(start, end).trim();
        }

        List<Condition> conditions = Collections.emptyList();
        if (wherePart != null && !wherePart.isEmpty()) {
            conditions = parseConditions(wherePart);
        }

        // --- 2) ORDER BY parsing ---
        String sortColumn = null;
        boolean descending = false;

        if (orderIdx != -1) {
            String orderPart = lower.substring(orderIdx + "order by".length()).trim();
            String[] tokens = orderPart.split("\\s+");
            if (tokens.length >= 1) {
                sortColumn = tokens[0].trim(); // e.g. "salary"
            }
            if (tokens.length >= 2) {
                String dir = tokens[1].trim();
                descending = dir.equalsIgnoreCase("desc")
                        || dir.equalsIgnoreCase("descending");
            }
        }

        // --- 3) TOP N parsing ---
        Integer topN = extractTopN(lower);

        // ======== QUERYDSL ROUTING ========

        // Case 0: no conditions / no ordering / no limit → just return all
        if (conditions.isEmpty() && sortColumn == null && topN == null) {
            return allRows;
        }

        // Case 1: one condition, no ORDER BY, no TOP → use QueryDSL equality filter
        if (conditions.size() == 1 && sortColumn == null && topN == null) {
            Condition c = conditions.get(0);
            return dataRowQueryService.findRowsByColumnValueUsingQuerydsl(
                    c.column, c.value
            );
        }

        // Case 2: two conditions, no ORDER BY, no TOP → QueryDSL AND of two columns
        if (conditions.size() == 2 && sortColumn == null && topN == null) {
            Condition c1 = conditions.get(0);
            Condition c2 = conditions.get(1);
            return dataRowQueryService.findRowsByTwoColumnsUsingQuerydsl(
                    c1.column, c1.value,
                    c2.column, c2.value
            );
        }

        // Case 3: one condition + ORDER BY + TOP → QueryDSL filter + sort + limit
        if (conditions.size() == 1 && sortColumn != null && topN != null && topN > 0) {
            Condition c = conditions.get(0);
            return dataRowQueryService.findRowsByColumnValueSortedLimitedUsingQuerydsl(
                    c.column,
                    c.value,
                    sortColumn,
                    descending,
                    topN
            );
        }

        // Fallback: anything more complex → old in-memory logic (so nothing breaks)
        return filterInMemory(allRows, conditions, sortColumn, descending, topN);
    }

    // ----------------- helper types & methods -----------------

    private static class Condition {
        final String column; // lowercased
        final String value;  // lowercased

        Condition(String column, String value) {
            this.column = column;
            this.value = value;
        }
    }

    /**
     * Parses text like:
     * "department is engineering and city is paris"
     * into a list of Condition(column, value).
     */
    private List<Condition> parseConditions(String wherePart) {
        List<Condition> conditions = new ArrayList<>();

        // split `a is b and c is d`
        String[] fragments = wherePart.split("\\s+and\\s+");
        for (String fragment : fragments) {
            String f = fragment.trim();

            String[] pieces;
            if (f.contains(" is ")) {
                pieces = f.split("\\s+is\\s+", 2);
            } else if (f.contains("=")) {
                pieces = f.split("=", 2);
            } else {
                continue; // unsupported fragment
            }

            if (pieces.length < 2) {
                continue;
            }

            String col = pieces[0].trim().toLowerCase(Locale.ROOT);   // "department"
            String val = pieces[1].trim().toLowerCase(Locale.ROOT);   // "engineering"

            if (!col.isEmpty() && !val.isEmpty()) {
                conditions.add(new Condition(col, val));
            }
        }

        return conditions;
    }

    private List<DataRow> filterInMemory(List<DataRow> allRows,
                                         List<Condition> conditions,
                                         String sortColumn,
                                         boolean descending,
                                         Integer topN) {

        List<DataRow> filtered = new ArrayList<>(allRows);

        // WHERE conditions
        if (conditions != null && !conditions.isEmpty()) {
            filtered = filtered.stream()
                    .filter(row -> matchesAllConditions(row, conditions))
                    .collect(Collectors.toList());
        }

        // ORDER BY
        if (sortColumn != null && !sortColumn.isEmpty()) {
            final String sortColFinal = sortColumn;
            Comparator<DataRow> cmp = Comparator.comparing(
                    row -> extractComparableValue(row, sortColFinal),
                    NaturalLanguageQueryService::compareValues
            );
            if (descending) {
                cmp = cmp.reversed();
            }

            filtered.sort(cmp);
        }

        // TOP N
        if (topN != null && topN > 0 && filtered.size() > topN) {
            filtered = new ArrayList<>(filtered.subList(0, topN));
        }

        return filtered;
    }

    private boolean matchesAllConditions(DataRow row, List<Condition> conditions) {
        List<DataCell> cells = row.getCells();
        if (cells == null || cells.isEmpty()) {
            return false;
        }

        for (Condition c : conditions) {
            boolean matched = cells.stream().anyMatch(cell -> {
                String colName = safeLower(cell.getColumnName());
                String val = safeLower(cell.getValue());
                return colName.equals(c.column) && val.equals(c.value);
            });

            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    /**
     * Extract value of a given column from a row, used for sorting.
     */
    private String extractComparableValue(DataRow row, String sortColumnLower) {
        String sortCol = sortColumnLower.toLowerCase(Locale.ROOT);
        for (DataCell cell : row.getCells()) {
            if (cell.getColumnName() != null &&
                    cell.getColumnName().equalsIgnoreCase(sortCol)) {
                return cell.getValue() == null ? "" : cell.getValue();
            }
        }
        return "";
    }

    /**
     * Compares two values; tries numeric comparison first, then falls back to string.
     */
    private static int compareValues(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";

        // Try numeric
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            return Double.compare(da, db);
        } catch (NumberFormatException ignored) {
            // Not numeric, fall back to lexicographic
        }

        return a.compareToIgnoreCase(b);
    }

    /**
     * Extracts "top N" from text like "show top 2 rows ...".
     */
    private Integer extractTopN(String lowerQuestion) {
        Pattern p = Pattern.compile("top\\s+(\\d+)");
        Matcher m = p.matcher(lowerQuestion);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}