# SFTP Downloader

This application downloads `.zip` and `.rar` files from an SFTP server every 30 minutes and stores metadata in PostgreSQL. Each download session is appended to `download-log.csv`.

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

The downloads will appear in the `downloads` folder. Progress is printed to the container logs.
