package com.nupur.csv_chat_graphql.repository;

import com.nupur.csv_chat_graphql.DataCell;
import com.nupur.csv_chat_graphql.DataRow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DataCellRepository extends JpaRepository<DataCell, Long> {

    List<DataCell> findByRow(DataRow row);
}