package com.client_app.repository;

import com.client_app.model.ImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportStatusRepository extends JpaRepository<ImportStatus, Long> {
}
