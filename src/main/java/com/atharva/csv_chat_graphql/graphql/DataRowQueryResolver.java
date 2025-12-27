package com.nupur.csv_chat_graphql.graphql;

import com.nupur.csv_chat_graphql.DataRow;
import com.nupur.csv_chat_graphql.service.DataRowQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DataRowQueryResolver {

    private final DataRowQueryService dataRowQueryService;

    public DataRowQueryResolver(DataRowQueryService dataRowQueryService) {
        this.dataRowQueryService = dataRowQueryService;
    }

    @QueryMapping
    public List<DataRow> rowsByColumnValue(@Argument String columnName,
                                           @Argument String value) {

        // 🔹 NOW using QueryDSL-based method
        List<DataRow> result =
                dataRowQueryService.findRowsByColumnValueUsingQuerydsl(columnName, value);

        return result != null ? result : List.of();
    }
}