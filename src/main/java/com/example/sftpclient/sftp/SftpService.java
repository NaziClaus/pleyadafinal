package com.example.sftpclient.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Vector;

@Service
public class SftpService {

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port:22}")
    private int port;

    @Value("${sftp.username}")
    private String username;

    @Value("${sftp.password}")
    private String password;

    @Value("${sftp.remote-path:/}")
    private String remotePath;

    public interface FileVisitor {
        void visit(ChannelSftp.LsEntry entry, String path) throws Exception;
    }

    private ChannelSftp connect() throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(10000);
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect(10000);
        return channel;
    }

    public void scan(FileVisitor visitor) throws Exception {
        ChannelSftp channel = connect();
        try {
            scanDir(channel, remotePath, visitor);
        } finally {
            channel.disconnect();
            channel.getSession().disconnect();
        }
    }

    private void scanDir(ChannelSftp channel, String path, FileVisitor visitor) throws Exception {
        Vector<ChannelSftp.LsEntry> entries = channel.ls(path);
        for (ChannelSftp.LsEntry entry : entries) {
            if (entry.getFilename().equals(".") || entry.getFilename().equals("..")) {
                continue;
            }
            String fullPath = path.endsWith("/") ? path + entry.getFilename() : path + "/" + entry.getFilename();
            if (entry.getAttrs().isDir()) {
                scanDir(channel, fullPath, visitor);
            } else {
                visitor.visit(entry, fullPath);
            }
        }
    }

    public void download(String remoteFile, File localFile) throws Exception {
        ChannelSftp channel = connect();
        try (InputStream in = channel.get(remoteFile);
             FileOutputStream out = new FileOutputStream(localFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } finally {
            channel.disconnect();
            channel.getSession().disconnect();
        }
    }
}
