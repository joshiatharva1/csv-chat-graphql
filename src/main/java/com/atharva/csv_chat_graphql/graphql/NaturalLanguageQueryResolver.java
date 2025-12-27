package com.nupur.csv_chat_graphql.graphql;

import com.nupur.csv_chat_graphql.DataRow;
import com.nupur.csv_chat_graphql.service.NaturalLanguageQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class NaturalLanguageQueryResolver {

    private final NaturalLanguageQueryService naturalLanguageQueryService;

    public NaturalLanguageQueryResolver(NaturalLanguageQueryService naturalLanguageQueryService) {
        this.naturalLanguageQueryService = naturalLanguageQueryService;
    }

    // Existing ask()
    @QueryMapping
    public List<DataRow> ask(@Argument String question) {
        return naturalLanguageQueryService.ask(question);
    }

    // New askSmart() — returns AskResult with optimizedQuery + rows
    @QueryMapping
    public AskResult askSmart(@Argument String question) {

        // Run the normal NL query
        List<DataRow> rows = naturalLanguageQueryService.ask(question);

        // Escape quotes
        String escaped = question.replace("\"", "\\\"");

        // Build the GraphQL optimized query string
        String optimizedQuery = """
                query {
                  ask(question: "%s") {
                    id
                    cells {
                      columnName
                      value
                    }
                  }
                }
                """.formatted(escaped);

        return new AskResult(optimizedQuery, rows);
    }
}