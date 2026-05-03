# My-Market-App

A server-side rendered marketplace web application: a product showcase with search/sorting/pagination, a shopping cart, and order placement with order history.

## Tech stack

- Java 21
- Spring Boot 3.5 (servlet stack), Spring Web MVC
- Thymeleaf
- Spring Data JPA / Hibernate
- PostgreSQL (runtime), H2 (tests)
- Maven (Maven Wrapper included)
- JUnit 5 / Spring Boot Test
- Docker, Docker Compose

## Product catalog loader

Products are loaded from an external CSV file on every application startup.

- **CSV file:**
   - Default file location: `./data/products.csv` — configurable via `app.catalog.csv-path` property.
   - Expected CSV header: `id,title,description,imgPath,price`.
   - Each product row is inserted only once — the first time it is encountered on the app startup.
- **Product image files:**
   - Default image directory: `./data/images/` — configurable via `app.catalog.images-path` property.
   - The images are served under `/images/**` API endpoint.
- **Disable:** set `app.catalog.load-on-startup=false` to skip the loader entirely.

Check the `data_example/` directory for a sample CSV and product image files. Copy it to `data/` to use it as a starting point.

### Adding new products

1. Append new rows to `data/products.csv`.
2. Drop the corresponding image files into `data/images/`.
3. Restart the application (or `docker compose restart app` when running in Docker).

## Shopping cart and user model

The application has no authentication. A single implicit user is assumed per browser session:

- The shopping cart lives in the HTTP session and is **NOT** persisted to the database — it is discarded when the browser session expires.
- Placed orders are global: every visitor sees every order (no user-specific order history).

## Build and run app locally

Build an executable jar and run the tests:

```bash
./mvnw clean package                # compile + test + produce an executable JAR
./mvnw test                         # run tests only
./mvnw clean package -DskipTests    # skip tests
```

To run the application against a local Postgres instance:

1. Start a local Postgres in Docker:
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```
2. Run the application with the `dev` profile, which automatically connects to the `docker-compose.dev.yml` Postgres instance:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. Open http://localhost:8080 in your browser.

Alternatively, the application can be run without using the `dev` profile by supplying the DB connection details directly through environment variables. For the same Postgres instance started with `docker-compose.dev.yml`:

```bash
DB_HOST=localhost DB_PORT=5432 DB_NAME=marketdb \
POSTGRES_USER=postgres POSTGRES_PASSWORD=postgres \
./mvnw spring-boot:run
```

Any other local Postgres instance can be used by adjusting the environment variable values accordingly.

## Run app in a Docker container

The root `docker-compose.yml` runs both the PostgreSQL and the application containers; the application container is built using the provided `Dockerfile`:

```bash
docker compose up -d --build  # (re)build the app image and start the containers
docker compose up -d          # start the containers without app image build (after the image build if no src code changes)
```

- The application is exposed on http://localhost:8080.
- The host directory `./data` is mounted read-only into the container at `/app/data`, so the catalog CSV and product images are located on the host.
- To update the catalog: edit host-side `data/products.csv` and `data/images/`, then `docker compose restart app`.