package com.example.sftpapp.util;

public class ProgressBar {
    private final int barLength = 50;

    public void update(String fileName, long totalBytes, long transferred) {
        double percent = (double) transferred / totalBytes;
        int filled = (int) (percent * barLength);
        StringBuilder bar = new StringBuilder();
        bar.append('\r').append(fileName).append(" [");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? '=' : ' ');
        }
        bar.append("] ");
        bar.append(String.format("%d%% %d/%d bytes", (int)(percent * 100), transferred, totalBytes));
        System.out.print(bar.toString());
        if (transferred >= totalBytes) {
            System.out.println();
        }
    }
}
