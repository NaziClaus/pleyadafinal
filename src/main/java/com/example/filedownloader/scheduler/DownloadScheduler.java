package com.example.filedownloader.scheduler;

import com.example.filedownloader.model.FileMetadata;
import com.example.filedownloader.service.SftpService;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class DownloadScheduler {

    private final SftpService sftpService;

    @Value("${CSV_LOG_DIR:logs}")
    private String logDir;

    public DownloadScheduler(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    @Scheduled(fixedRateString = "PT30M")
    public void scan() {
        try {
            List<FileMetadata> downloaded = sftpService.scanAndDownload();
            if (!downloaded.isEmpty()) {
                writeCsv(downloaded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeCsv(List<FileMetadata> list) {
        new java.io.File(logDir).mkdirs();
        java.io.File file = new java.io.File(logDir, "download-log.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))) {
            for (FileMetadata meta : list) {
                writer.writeNext(new String[]{meta.getFileName(), String.valueOf(meta.getSize()), meta.getDownloadedAt().toString()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
