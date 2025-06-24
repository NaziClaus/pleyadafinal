package com.example.filedownloader.service;

import com.example.filedownloader.model.FileMetadata;
import com.example.filedownloader.repository.FileMetadataRepository;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Service
public class SftpService {

    @Value("${SFTP_HOST}")
    private String host;
    @Value("${SFTP_PORT:22}")
    private int port;
    @Value("${SFTP_USER}")
    private String user;
    @Value("${SFTP_PASS}")
    private String password;
    @Value("${SFTP_REMOTE_DIR:/}")
    private String remoteDir;
    @Value("${LOCAL_DOWNLOAD_DIR:downloads}")
    private String localDir;

    private final FileMetadataRepository repository;

    private Session session;
    private ChannelSftp channel;

    public SftpService(FileMetadataRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        new File(localDir).mkdirs();
    }

    @PreDestroy
    public void destroy() {
        if (channel != null && channel.isConnected()) channel.disconnect();
        if (session != null && session.isConnected()) session.disconnect();
    }

    public List<FileMetadata> scanAndDownload() throws Exception {
        List<FileMetadata> downloaded = new ArrayList<>();
        Vector<ChannelSftp.LsEntry> files = channel.ls(remoteDir);
        for (ChannelSftp.LsEntry entry : files) {
            String name = entry.getFilename();
            if (entry.getAttrs().isDir()) continue;
            if (!name.endsWith(".zip") && !name.endsWith(".rar")) continue;
            if (repository.existsByFileName(name)) continue;
            long size = entry.getAttrs().getSize();
            File target = new File(localDir, name);
            try (OutputStream os = new FileOutputStream(target)) {
                channel.get(remoteDir + "/" + name, os, new ProgressMonitor(name, size));
            }
            FileMetadata meta = new FileMetadata(name, size, Instant.now());
            repository.save(meta);
            downloaded.add(meta);
        }
        return downloaded;
    }

    private static class ProgressMonitor implements SftpProgressMonitor {
        private final String fileName;
        private final long max;
        private long count;

        ProgressMonitor(String fileName, long max) {
            this.fileName = fileName;
            this.max = max;
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            System.out.printf("Starting %s (size: %d bytes)\n", fileName, max);
        }

        @Override
        public boolean count(long bytes) {
            count += bytes;
            int percent = (int) ((count * 100) / max);
            System.out.printf("\r%s: %d%%", fileName, percent);
            return true;
        }

        @Override
        public void end() {
            System.out.printf("\nFinished %s\n", fileName);
        }
    }
}
