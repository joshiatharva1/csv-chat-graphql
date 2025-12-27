package com.nupur.csv_chat_graphql;

import jakarta.persistence.*;

@Entity
public class DataCell {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String columnName;

    // Physical DB column name is "cell_value" (NOT "value" → avoids H2 keyword)
    @Column(name = "cell_value", length = 2000)
    private String cellValue;

    @ManyToOne
    @JoinColumn(name = "row_id")
    private DataRow row;

    // JPA needs a protected / default constructor
    protected DataCell() {
    }

    public DataCell(String columnName, String value, DataRow row) {
        this.columnName = columnName;
        this.cellValue = value;
        this.row = row;
    }

    public Long getId() {
        return id;
    }

    public String getColumnName() {
        return columnName;
    }

    public DataRow getRow() {
        return row;
    }

    public void setRow(DataRow row) {
        this.row = row;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    // ---- IMPORTANT PART ----
    // We support ALL possible getter/setter names
    // so any old code still works and populates the same field.

    // Used by GraphQL for "value" field
    public String getValue() {
        return cellValue;
    }

    // If somewhere in the code we did setValue(...)
    public void setValue(String value) {
        this.cellValue = value;
    }

    // If somewhere we used getCellValue()
    public String getCellValue() {
        return cellValue;
    }

    // If somewhere we used setCellValue(...)
    public void setCellValue(String value) {
        this.cellValue = value;
    }
}