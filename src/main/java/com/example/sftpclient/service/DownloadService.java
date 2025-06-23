package com.example.sftpclient.service;

import com.example.sftpclient.model.RemoteFile;
import com.example.sftpclient.repository.RemoteFileRepository;
import com.example.sftpclient.sftp.SftpService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

@Service
public class DownloadService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    private final SftpService sftpService;
    private final RemoteFileRepository repository;
    private final Queue<RemoteFile> queue = new ArrayDeque<>();

    @Value("${local.download-dir}")
    private String downloadDir;

    @Value("${csv.log-path}")
    private String logPath;

    private Gauge queueGauge;
    private Gauge currentFileGauge;

    private RemoteFile current;

    public DownloadService(SftpService sftpService, RemoteFileRepository repository, MeterRegistry meterRegistry) {
        this.sftpService = sftpService;
        this.repository = repository;
        queueGauge = Gauge.builder("sftp.queue.size", queue::size).register(meterRegistry);
        currentFileGauge = Gauge.builder("sftp.current.file.size", () -> current == null ? 0 : current.getSize()).register(meterRegistry);
    }

    @PostConstruct
    public void init() {
        new File(downloadDir).mkdirs();
    }

    @Scheduled(fixedDelay = 1800000)
    public void scanAndDownload() {
        try {
            sftpService.scan((entry, path) -> {
                if (!path.endsWith(".zip") && !path.endsWith(".rar")) {
                    return;
                }
                repository.findByPath(path).orElseGet(() -> {
                    RemoteFile rf = new RemoteFile(path, entry.getAttrs().getSize(), entry.getAttrs().getMTime() * 1000L);
                    repository.save(rf);
                    queue.add(rf);
                    return rf;
                });
            });
            processQueue();
        } catch (Exception e) {
            logger.error("Failed to scan/download", e);
        }
    }

    private void processQueue() throws Exception {
        while (!queue.isEmpty()) {
            current = queue.poll();
            File localFile = new File(downloadDir, new File(current.getPath()).getName());
            logger.info("Downloading {} to {}", current.getPath(), localFile);
            sftpService.download(current.getPath(), localFile);
            current.setDownloaded(true);
            repository.save(current);
            appendLog(current);
        }
        current = null;
    }

    private void appendLog(RemoteFile file) throws IOException {
        File csv = new File(logPath);
        csv.getParentFile().mkdirs();
        boolean exists = csv.exists();
        try (FileWriter writer = new FileWriter(csv, true)) {
            if (!exists) {
                writer.write("path,date,size\n");
            }
            writer.write(String.format("%s,%s,%d\n", file.getPath(), Instant.now(), file.getSize()));
        }
    }
}
