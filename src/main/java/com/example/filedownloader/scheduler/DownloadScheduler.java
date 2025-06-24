package com.example.filedownloader.scheduler;

import com.example.filedownloader.model.FileMetadata;
import com.example.filedownloader.service.SftpService;
import com.opencsv.CSVWriter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Component
public class DownloadScheduler {

    private final SftpService sftpService;

    public DownloadScheduler(SftpService sftpService) {
        this.sftpService = sftpService;
    }

    @Scheduled(fixedRateString = "PT30M")
    public void scan() throws Exception {
        List<FileMetadata> downloaded = sftpService.scanAndDownload();
        if (!downloaded.isEmpty()) {
            writeCsv(downloaded);
        }
    }

    private void writeCsv(List<FileMetadata> list) {
        try (CSVWriter writer = new CSVWriter(new FileWriter("download-log.csv", true))) {
            for (FileMetadata meta : list) {
                writer.writeNext(new String[]{meta.getFileName(), String.valueOf(meta.getSize()), meta.getDownloadedAt().toString()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
