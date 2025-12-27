package com.nupur.csv_chat_graphql.service;

import com.nupur.csv_chat_graphql.DataCell;
import com.nupur.csv_chat_graphql.DataRow;
import com.nupur.csv_chat_graphql.repository.DataCellRepository;
import com.nupur.csv_chat_graphql.repository.DataRowRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@Service
public class CsvService {

    private final DataRowRepository dataRowRepository;
    private final DataCellRepository dataCellRepository;

    public CsvService(DataRowRepository dataRowRepository,
                      DataCellRepository dataCellRepository) {
        this.dataRowRepository = dataRowRepository;
        this.dataCellRepository = dataCellRepository;
    }

    @Transactional
    public void importCsv(MultipartFile file) throws IOException, CsvValidationException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV file is empty");
        }

        // 🔥 IMPORTANT: Clear previous CSV data so each upload starts fresh
        // Because DataRow has cascade = ALL + orphanRemoval, deleting rows
        // will also delete the linked DataCell records.
        dataRowRepository.deleteAll();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            if (header == null || header.length == 0) {
                throw new IllegalArgumentException("CSV file has no header row.");
            }

            String[] rowValues;
            while ((rowValues = csvReader.readNext()) != null) {
                // Create one DataRow per CSV row
                DataRow row = new DataRow();
                dataRowRepository.save(row);

                for (int i = 0; i < header.length; i++) {
                    String columnName = header[i];
                    String value = (i < rowValues.length) ? rowValues[i] : "";

                    DataCell cell = new DataCell(columnName, value, row);
                    dataCellRepository.save(cell);
                }
            }
        }
    }
}