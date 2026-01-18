package com.client_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class CsvAsyncService {
    private final CsvImportService csvImportService;

    @Async
    public void importCsvAsync(InputStream csvInputStream) {
        csvImportService.importCsv(csvInputStream);
    }
}
