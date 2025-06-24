# SFTP Downloader

This application downloads `.zip` and `.rar` files from an SFTP server every 30 minutes and stores metadata in PostgreSQL. Only files whose **last modified** date matches the current system date are fetched. Each download session is appended to a CSV log. The SFTP connection is opened at each scan so the service keeps running even if the server is temporarily unreachable.

## Usage

1. Build the application:

```bash
./mvnw package
```

2. Place the resulting `target/filedownloader-0.0.1-SNAPSHOT.jar`, `docker-compose.yml`, and `.env` in the same directory.
3. Run Docker Compose:

```bash
docker-compose up --build
```

The downloaded archives are saved in the folder specified by the `LOCAL_DOWNLOAD_DIR` variable (default `./downloads`). CSV logs are written to the directory set by `CSV_LOG_DIR` (default `./logs`). For example on Windows you could mount `D:\Downloads` and `D:\Downloads\Logs`.
Progress, including speed and estimated time remaining, is printed to the container logs and flushed for real-time updates.
