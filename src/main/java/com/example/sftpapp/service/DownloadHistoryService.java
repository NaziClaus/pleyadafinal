package com.example.sftpapp.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

@Service
public class DownloadHistoryService {
    private static final String HISTORY_FILE = "download_history.xlsx";
    private final Set<String> downloaded = new HashSet<>();
    private Workbook workbook;
    private Sheet sheet;

    @PostConstruct
    public void init() throws IOException {
        File file = new File(HISTORY_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
            }
        } else {
            workbook = new XSSFWorkbook();
        }
        if (workbook.getNumberOfSheets() == 0) {
            sheet = workbook.createSheet("history");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Filename");
            header.createCell(1).setCellValue("LocalPath");
            header.createCell(2).setCellValue("Timestamp");
        } else {
            sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    downloaded.add(row.getCell(0).getStringCellValue());
                }
            }
        }
    }

    public boolean isDownloaded(String fileName) {
        return downloaded.contains(fileName);
    }

    public synchronized void recordDownload(String fileName, Path localPath) throws IOException {
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(fileName);
        row.createCell(1).setCellValue(localPath.toString());
        row.createCell(2).setCellValue(System.currentTimeMillis());
        downloaded.add(fileName);
        try (FileOutputStream fos = new FileOutputStream(HISTORY_FILE)) {
            workbook.write(fos);
        }
    }
}
