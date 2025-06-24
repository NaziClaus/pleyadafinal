package com.example.filedownloader.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private long size;

    private Instant downloadedAt;

    public FileMetadata() {
    }

    public FileMetadata(String fileName, long size, Instant downloadedAt) {
        this.fileName = fileName;
        this.size = size;
        this.downloadedAt = downloadedAt;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getDownloadedAt() {
        return downloadedAt;
    }

    public void setDownloadedAt(Instant downloadedAt) {
        this.downloadedAt = downloadedAt;
    }
}
