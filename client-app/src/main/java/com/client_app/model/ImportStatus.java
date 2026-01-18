package com.client_app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
public class ImportStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Status status;
    private LocalDateTime creationTime;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private long processedRows;


    public ImportStatus() {
        this.status = Status.CREATED;
        this.creationTime = LocalDateTime.now();
        this.processedRows = 0;
    }
}
