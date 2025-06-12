package com.example.sftpapp.service;

import com.example.sftpapp.config.SftpProperties;
import com.example.sftpapp.util.ProgressBar;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Vector;

@Service
public class SftpDownloader {
    private final SftpProperties properties;
    private final DownloadHistoryService historyService;

    public SftpDownloader(SftpProperties properties, DownloadHistoryService historyService) {
        this.properties = properties;
        this.historyService = historyService;
    }

    public void run() throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(properties.getUser(), properties.getHost(), properties.getPort());
        session.setPassword(properties.getPassword());
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        Files.createDirectories(Path.of(properties.getLocalPath()));

        Vector<ChannelSftp.LsEntry> files = channel.ls(properties.getRemotePath());
        LocalDate today = LocalDate.now();
        ProgressBar bar = new ProgressBar();

        for (ChannelSftp.LsEntry entry : files) {
            LocalDate fileDate = Instant.ofEpochSecond(entry.getAttrs().getMTime())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            if (!fileDate.equals(today)) {
                continue;
            }
            String fileName = entry.getFilename();
            if (historyService.isDownloaded(fileName)) {
                continue;
            }
            long size = entry.getAttrs().getSize();
            Path localFile = Path.of(properties.getLocalPath(), fileName);
            try (InputStream in = channel.get(properties.getRemotePath() + "/" + fileName);
                 FileOutputStream out = new FileOutputStream(localFile.toFile())) {
                byte[] buffer = new byte[4096];
                long transferred = 0;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                    transferred += read;
                    bar.update(fileName, size, transferred);
                }
            }
            historyService.recordDownload(fileName, localFile);
        }
        channel.disconnect();
        session.disconnect();
    }
}
