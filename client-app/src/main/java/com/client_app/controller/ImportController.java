package com.client_app.controller;

import com.client_app.model.ImportStatus;
import com.client_app.repository.ImportStatusRepository;
import com.client_app.service.CsvAsyncService;
import com.client_app.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class ImportController {
    private final CsvImportService csvImportService;
    private final ImportStatusRepository importStatusRepository;
    private final CsvAsyncService csvAsyncService;

    @PostMapping("/import")
    public ResponseEntity<?> startImport(@RequestParam("file") MultipartFile file) throws IOException {
        csvAsyncService.importCsvAsync(file.getInputStream());
        return ResponseEntity.ok(Collections.singletonMap("message", "Import rozpoczęty"));
    }

    @GetMapping("/import/status")
    public ImportStatus getImportStatus() {
        return importStatusRepository.findById(1l).orElseThrow();
    }
}
