# SFTP Client

This Spring Boot application periodically scans an SFTP server for new `zip` and `rar` files, stores metadata in PostgreSQL, downloads missing files and exposes Prometheus metrics.

## Usage

1. Build the jar:
   ```bash
   ./mvnw package
   ```
2. Copy `docker-compose.yml`, `.env`, and the generated jar (`target/*.jar`) into a directory.
3. Run `docker-compose up`.

Prometheus will be available on port `9090`.
