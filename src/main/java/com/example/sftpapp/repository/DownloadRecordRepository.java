package com.example.sftpapp.repository;

import com.example.sftpapp.entity.DownloadRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadRecordRepository extends JpaRepository<DownloadRecord, Long> {
    boolean existsByFilename(String filename);
}
