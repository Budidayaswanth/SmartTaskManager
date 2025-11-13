<context>
I will add a comprehensive README for project setup, deployment, and integration.
</context>

## SmartTask Backend

RESTful Spring Boot service powering the SmartTask applications. It exposes authentication, token management, and task APIs with JWT-based security.

### Tech Stack
- Java & Spring Boot (`spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`)
- PostgreSQL with Flyway migrations
- JWT authentication
- Springdoc / Swagger UI for API documentation

### Project Structure
Key directories:
- `src/main/java/com/smarttask/smarttask_backend/`
  - `controller/` – REST endpoints (`AuthController`, `TaskController`, `SwaggerAuthController`)
  - `service/` – Business logic (`AuthService`, `TaskService`)
  - `repository/` – Spring Data repositories
  - `security/` – JWT filter/service integration
  - `config/` – Security and OpenAPI configuration
  - `exception/` – Global exception handler
- `src/main/resources/`
  - `application.yaml` – env-specific configuration (base, `dev`, `prod`)
  - `db/migration/` – Flyway SQL migrations (`V1__init.sql`)
  - `static/swagger-config.js` – Swagger auto-login script

### Prerequisites
- Java 21 (check with `java -version`)
- Maven 3.9+ (`mvn -version`)
- PostgreSQL 14+ (for local dev profile)

### Running Locally
```bash
# 1. Start postgres manually or via docker compose (if you have it).
# 2. From the project root:

export SPRING_PROFILES_ACTIVE=dev         # Windows PowerShell: $env:SPRING_PROFILES_ACTIVE="dev"
mvn clean package                         # Optional compile check
mvn spring-boot:run                       # Launches on http://localhost:8080
```

- After startup browse to <http://localhost:8080/swagger>.
- To stop the app press `Ctrl+C`.

> IDE users: configure the run profile with environment variable `SPRING_PROFILES_ACTIVE=dev` then run the `SmarttaskBackendApplication` class.

### Configuration (`application.yaml`)
- Base section: shared defaults (logging, JWT issuer, Swagger paths).
- `dev` profile (activated via `SPRING_PROFILES_ACTIVE=dev`):
  - Local Postgres URLs and credentials.
  - JWT dev secret.
- `prod` profile (activated via `SPRING_PROFILES_ACTIVE=prod`, default for Render):
  - Uses environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`.
  - Optional `swagger.server-url` for hosted Swagger docs.

### Environment Variables (Render deployment)
Set under Render → **Environment**:
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET` (Base64, 256 bits)
- Optional: `swagger.server-url` (e.g. `https://smarttaskmanager-s0fd.onrender.com`)

### Database Migration
Flyway runs automatically on startup. The `V1__init.sql` migration creates:
- `users` table with UUID primary keys, unique username/email, status flags.
- `tasks` table linked to users.
- `refresh_tokens` table with foreign key to users.

### API Overview
- `POST /api/auth/register` – Create user. Returns 201 with `UserResponse`.
- `POST /api/auth/login` – Returns JWT access/refresh tokens. 400 on invalid credentials, 403 if disabled.
- `POST /api/auth/refresh` – Issues new access + refresh token.
- `POST /api/auth/logout` – Revokes refresh tokens for authenticated user.
- `GET /api/tasks` – List tasks for the authenticated user.
- `POST /api/tasks` – Create task.
- `PUT /api/tasks/{id}` – Update task.
- `DELETE /api/tasks/{id}` – Delete task.

All task endpoints require `Authorization: Bearer <accessToken>`.

### Swagger Usage
1. Visit `/swagger` (local or hosted).
2. `swagger-config.js` auto-authenticates by calling `/api/auth/swagger-login`.
3. Use the UI to explore endpoints; requests include the JWT automatically.
4. To change Swagger auto-login credentials, update `swagger.auth.*` in `application.yaml`.

### Flutter Integration
1. Set API `baseUrl` to the deployed domain (`https://smarttaskmanager-s0fd.onrender.com`).
2. `POST /api/auth/register` to create accounts (handle 400/409/500 responses).
3. `POST /api/auth/login` to obtain `{ accessToken, refreshToken, tokenType }`.
4. Attach `Authorization: Bearer <accessToken>` to subsequent calls.
5. Refresh token via `/api/auth/refresh?refreshToken=...` when 401 indicates expiration.
6. Use `/api/auth/logout` to invalidate refresh tokens.
7. Tasks endpoints map directly to CRUD operations on the authenticated user.

### Testing Checklist
- Register → 201
- Login (valid/invalid credentials) → 200 / 400 / 403
- Refresh token → 200
- Task CRUD with/without token → 200 / 401
- Swagger auto-login on hosted and local environments.

### Troubleshooting
- App fails to start locally → ensure `SPRING_PROFILES_ACTIVE=dev` and Postgres is running.
- Register returns 500 on Render → ensure DB env vars are set, check logs for constraint violations (now logged via `GlobalExceptionHandler`).
- Swagger still uses localhost → set `swagger.server-url` env variable.
- `400 Invalid credentials` → confirm user exists or register first.

### Deployment (Render)
```bash
# Render handles builds, but you can test locally with prod profile if needed:
SPRING_PROFILES_ACTIVE=prod \
DB_URL="jdbc:postgresql://...." \
DB_USER="..." \
DB_PASSWORD="..." \
JWT_SECRET="..." \
mvn spring-boot:run
```

1. Connect the GitHub repo in Render, choose build command `./mvnw clean package` and start command `java -jar target/smarttask-backend-0.0.1-SNAPSHOT.jar`.
2. Set the environment variables in Render → Environment (see list above).
3. Deploy; watch the logs for Flyway migration success and `Tomcat started on port 8080`.
4. Smoke-test the hosted APIs via `<render-url>/swagger`.

### License
MIT (adjust if needed).

### this terminal command is to run swagger in locally
 mvn spring-boot:run -Dspring-boot.run.profiles=dev 