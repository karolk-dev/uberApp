package com.client_app.controller;

import com.client_app.exceptions.CsvImportException;
import com.client_app.model.ImportStatus;
import com.client_app.model.Status;
import com.client_app.repository.ClientRepository;
import com.client_app.repository.ImportStatusRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.FileCopyUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public class CsvImportIntegrationTest extends DatabaseContainer {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ImportStatusRepository importStatusRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    public void testCsvImport() throws Exception {

        ClassPathResource resource = new ClassPathResource("clients2.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/import")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(status().isOk());

        Thread.sleep(10000);
        String username = clientRepository.findByUuid("9c84f878-7207-4a86-a976-272cfbd99b6b").orElseThrow().getUsername();
        assertThat(username).isEqualTo("user0");
        ImportStatus status = importStatusRepository.findById(1L).orElseThrow();
        assertThat(status.getStatus()).isEqualTo(Status.COMPLETED);

    }

    @Test
    public void testCsvImportError() throws Exception {

        ClassPathResource resource = new ClassPathResource("clients2.csv");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());
        CountDownLatch startLatch = new CountDownLatch(1);

        Runnable importTask = () -> {
            try {
                startLatch.await();

                mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/import")
                                .file("file", fileContent)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                ).andDo(print());
            } catch (Exception e) {
                throw new CsvImportException("Błąd podczas importu CSV", e);
            }
        };

        Thread t1 = new Thread(importTask);
        Thread t2 = new Thread(importTask);
        t1.start();
        t2.start();

        startLatch.countDown();

        t1.join();
        t2.join();

        List<ImportStatus> allStatuses = importStatusRepository.findAll();
        assertEquals(1, allStatuses.size(), "");
        assertEquals(Status.COMPLETED, allStatuses.get(0).getStatus(), "");
    }
}
