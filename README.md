# DevLog Platform Service

Spring Boot backend for a developer blogging platform.

## Stack
- Java 21
- Spring Boot 3
- Spring Data JPA
- Spring Security + JWT
- Flyway
- H2 in-memory database for local development

## Run Backend

From:
`/Users/priyankakalamegam/Project/blog-app/service/blog-app-service`

Build:
```bash
gradle clean build
```

Run:
```bash
java -jar build/libs/blog-app-service-0.0.1-SNAPSHOT.jar --server.port=8081
```

## Demo Login Details

- Admin: `admin` / `Admin@123`
- User: `priyanka` / `Priyanka@123`

## Local Database

The default local profile uses H2 in memory:

- JDBC URL: `jdbc:h2:mem:blogdb`
- Username: `sa`
- Password: empty
- Console: `http://localhost:8081/h2-console`

## Environment Variables
- `JWT_SECRET` (default placeholder, set secure value in production)
- `JWT_EXPIRATION_SECONDS` (default: `86400`)

## Key API Groups
- Auth: `/api/auth/*`
- Posts: `/api/posts/*`
- Tags: `/api/tags`
- Profiles: `/api/profiles/*`
- Resume: `/api/resume/*`
- Dashboard: `/api/dashboard/me`
- Admin: `/api/admin/overview`

## DB Migration
- Flyway migration file:
  - `src/main/resources/db/migration/V1__init_developer_platform.sql`
