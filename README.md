## Running with Docker

This project can be run locally with Docker using a Spring Boot container and a MySQL container.

### Requirements

* Docker Desktop
* Docker Compose

### Environment variables

Create a local `.env` file from the example file:

```bash
cp .env.example .env
```

Then update the values in `.env` if needed.

The real `.env` file is ignored by Git and should not be committed.

### Start the application

From the project root, run:

```bash
docker compose up --build
```

This starts:

* Spring Boot API container
* MySQL 8.0 container
* Persistent MySQL Docker volume

The API will be available at:

```text
http://localhost:8080
```

### MySQL connection

The MySQL container is exposed to the host machine on port `3307`.

For MySQL Workbench or another database client:

```text
Host: 127.0.0.1
Port: 3307
Database: goodreads_db
Username: goodreads_user
Password: value from .env
```

Inside Docker, the Spring Boot app connects to MySQL using the Compose service name:

```text
jdbc:mysql://mysql:3306/goodreads_db
```

### Stop the containers

```bash
docker compose down
```

### Stop containers and delete the database volume

```bash
docker compose down -v
```

Warning: this deletes the local Dockerized database data.
