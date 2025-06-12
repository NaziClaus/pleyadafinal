package com.example.sftpapp.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {
    private final SftpDownloader downloader;

    public Runner(SftpDownloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public void run(String... args) throws Exception {
        downloader.run();
    }
}
