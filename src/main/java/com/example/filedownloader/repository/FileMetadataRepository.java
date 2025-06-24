package com.example.filedownloader.repository;

import com.example.filedownloader.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    boolean existsByFileName(String fileName);
}
