package com.nupur.csv_chat_graphql;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class DataRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One row has many cells
    @OneToMany(mappedBy = "row", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DataCell> cells = new ArrayList<>();

    public DataRow() {
    }

    public Long getId() {
        return id;
    }

    // GraphQL will call this when you query "cells"
    public List<DataCell> getCells() {
        return cells;
    }

    // Helper method (optional, but nice to have)
    public void addCell(DataCell cell) {
        cells.add(cell);
        // make sure both sides are in sync
        // (only needed if you use this method)
    }
}