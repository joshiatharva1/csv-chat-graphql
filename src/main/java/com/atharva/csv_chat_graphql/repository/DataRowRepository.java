/*package com.nupur.csv_chat_graphql.repository;

import com.nupur.csv_chat_graphql.DataRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataRowRepository extends JpaRepository<DataRow, String> {
}*/
package com.nupur.csv_chat_graphql.repository;

import com.nupur.csv_chat_graphql.DataRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface DataRowRepository
        extends JpaRepository<DataRow, Long>,
        QuerydslPredicateExecutor<DataRow> {
}