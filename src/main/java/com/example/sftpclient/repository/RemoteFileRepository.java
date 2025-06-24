package com.example.sftpclient.repository;

import com.example.sftpclient.model.RemoteFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RemoteFileRepository extends JpaRepository<RemoteFile, Long> {
    Optional<RemoteFile> findByPath(String path);
}
