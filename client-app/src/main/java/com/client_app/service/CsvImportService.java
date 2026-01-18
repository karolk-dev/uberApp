package com.client_app.service;

import com.client_app.exceptions.CsvImportException;
import com.client_app.exceptions.ImportAlreadyRunningException;
import com.client_app.model.ImportStatus;
import com.client_app.model.Status;
import com.client_app.repository.ImportStatusRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class CsvImportService {

    private final DataSource dataSource;

    private final RedissonClient redissonClient;
    private final ImportStatusRepository importStatusRepository;

    @Transactional
    public void importCsv(InputStream csvInputStream) {
        RLock lock = redissonClient.getLock("importCsvLock");
        boolean locked = false;

        try {
            locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                throw new ImportAlreadyRunningException("Import już trwa");
            }

            ImportStatus status = ImportStatus.builder()
                    .startTime(LocalDateTime.now())
                    .status(Status.RUNNING)
                    .build();
            importStatusRepository.save(status);

            Connection connection = dataSource.getConnection();

            try (Reader fileReader = new BufferedReader(new InputStreamReader(csvInputStream))) {
                Reader countingReader = new CountingReader(fileReader, status);
                CopyManager copyManager = new CopyManager(connection.unwrap(BaseConnection.class));
                String copySql = "COPY clients (uuid, username, email, role, customer_id) "
                        + "FROM STDIN WITH (FORMAT csv, HEADER true)";
                long rowsImported = copyManager.copyIn(copySql, countingReader);

                status.setProcessedRows(rowsImported);
                status.setFinishTime(LocalDateTime.now());
                status.setStatus(Status.COMPLETED);
                importStatusRepository.save(status);
            }

        } catch (Exception e) {
            log.error("Blad podczas importu: {}", e.getMessage(), e);
            throw new CsvImportException("Import CSV nie powiodl sie", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }
}
