package com.nupur.csv_chat_graphql.graphql;

import com.nupur.csv_chat_graphql.DataRow;

import java.util.List;

public class AskResult {

    private final String optimizedQuery;
    private final List<DataRow> rows;

    public AskResult(String optimizedQuery, List<DataRow> rows) {
        this.optimizedQuery = optimizedQuery;
        this.rows = rows;
    }

    public String getOptimizedQuery() {
        return optimizedQuery;
    }

    public List<DataRow> getRows() {
        return rows;
    }
}