# SFTP App

Simple Spring Boot application to download files from an SFTP server filtered by today's date. The application stores download history in an Excel file and shows a real-time progress bar in the console. Downloaded file metadata is also stored in a PostgreSQL database.

## Configuration
Edit `src/main/resources/application.properties` with SFTP connection details and paths.

## Build and Run
```bash
mvn package
java -jar target/sftp-app-0.1.0.jar
```

## Docker Compose
To run the application with PostgreSQL using Docker Compose:

```bash
docker-compose up --build
```
