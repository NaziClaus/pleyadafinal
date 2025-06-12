package com.example.sftpapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sftp")
public class SftpProperties {
    private String host;
    private int port = 22;
    private String user;
    private String password;
    private String remotePath;
    private String localPath = "downloads";

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRemotePath() { return remotePath; }
    public void setRemotePath(String remotePath) { this.remotePath = remotePath; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
}
