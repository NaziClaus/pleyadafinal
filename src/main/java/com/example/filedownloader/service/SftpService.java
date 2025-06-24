package com.example.filedownloader.service;

import com.example.filedownloader.model.FileMetadata;
import com.example.filedownloader.repository.FileMetadataRepository;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
        LocalDate today = LocalDate.now();
        int queue = 0;
        for (ChannelSftp.LsEntry entry : files) {
            String name = entry.getFilename();
            if (entry.getAttrs().isDir()) continue;
            if (!name.endsWith(".zip") && !name.endsWith(".rar")) continue;
            LocalDate fileDate = Instant.ofEpochSecond(entry.getAttrs().getMTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (!fileDate.equals(today)) continue;
            if (repository.existsByFileName(name)) continue;
            queue++;
        }
        System.out.printf("Found %d files to download\n", queue);
        for (ChannelSftp.LsEntry entry : files) {
            String name = entry.getFilename();
            if (entry.getAttrs().isDir()) continue;
            if (!name.endsWith(".zip") && !name.endsWith(".rar")) continue;
            LocalDate fileDate = Instant.ofEpochSecond(entry.getAttrs().getMTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (!fileDate.equals(today)) continue;
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
        private long start;
        private long lastPrint;

        ProgressMonitor(String fileName, long max) {
            this.fileName = fileName;
            this.max = max;
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            start = System.currentTimeMillis();
            lastPrint = start;
            System.out.printf("Starting %s (size: %d bytes)\n", fileName, max);
        }

        @Override
        public boolean count(long bytes) {
            count += bytes;
            long now = System.currentTimeMillis();
            int percent = (int) ((count * 100) / max);
            double speed = (now - start) > 0 ? (double) count / (now - start) * 1000 : 0;
            long remaining = max - count;
            long eta = speed > 0 ? (long) (remaining / speed) : -1;
            if (now - lastPrint > 1000) {
                if (eta >= 0) {
                    System.out.printf("\r%s: %d%% (%.1f MB/s, %ds left)", fileName, percent, speed / 1048576.0, eta);
                } else {
                    System.out.printf("\r%s: %d%%", fileName, percent);
                }
                lastPrint = now;
            }
            return true;
        }

        @Override
        public void end() {
            long duration = System.currentTimeMillis() - start;
            System.out.printf("\nFinished %s in %ds\n", fileName, duration / 1000);
        }
    }
}
