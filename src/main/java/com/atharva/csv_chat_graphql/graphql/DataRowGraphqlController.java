package com.nupur.csv_chat_graphql.graphql;

import com.nupur.csv_chat_graphql.DataRow;
import com.nupur.csv_chat_graphql.repository.DataRowRepository;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DataRowGraphqlController {

    private final DataRowRepository dataRowRepository;

    public DataRowGraphqlController(DataRowRepository dataRowRepository) {
        this.dataRowRepository = dataRowRepository;
    }

    @QueryMapping
    public List<DataRow> rows() {
        List<DataRow> allRows = dataRowRepository.findAll();
        // GraphQL field is non-null → never return null
        return allRows != null ? allRows : List.of();
    }
}