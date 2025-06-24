# SFTP Client

This Spring Boot application periodically scans an SFTP server for new `zip` and `rar` files, stores metadata in PostgreSQL, downloads missing files and exposes Prometheus metrics.

The project uses the Maven Wrapper (`mvnw`) so you don't need Maven installed. Running `./mvnw` automatically downloads the correct Maven version.

## Usage

1. Build the jar:
   ```bash
   ./mvnw package
   ```
2. Copy `docker-compose.yml`, `.env`, and the generated jar (`target/*.jar`) into a directory.
3. Run `docker-compose up`.

Prometheus will be available on port `9090`.

### Configuration

Create a `.env` file next to `docker-compose.yml` with your credentials. See
`.env.example` for the required variables. At a minimum you must provide SFTP
connection details and database credentials. The file should look like:

```dotenv
# database
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sftp
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# SFTP credentials
SFTP_HOST=sftp
SFTP_PORT=22
SFTP_USERNAME=user
SFTP_PASSWORD=pass
```
