# Online Shop

Order management system for an e-commerce process, developed as part of CSS-3008 Corporate Development labs.

## Project Overview

Online Shop manages orders through a controlled lifecycle. It demonstrates business rule validation, Spring Boot dependency injection, JPA/Hibernate persistence, Flyway migrations, a REST contract, JWT security, and transaction management.

**Order lifecycle:**

`CART → PAID → SHIPPED → DELIVERED`

A return is handled as a separate process.

## Business Rules

| From | To | Allowed? |
|---|---|---|
| `CART` | `PAID` | Yes |
| `PAID` | `SHIPPED` | Yes |
| `DELIVERED` | `RETURNED` | No |
| `SHIPPED` | `CART` | No |

### Forbidden Transitions

1. **`DELIVERED → RETURNED`** — A delivered order cannot be returned directly. It must go through a separate return process.
2. **`SHIPPED → CART`** — A shipped order is already in logistics and cannot be moved back to the cart.

## Authorization Rules (Lab 5)

Roles: **CLERK** (junior) and **MANAGER** (senior). Locked action: **`pay`** (`CART → PAID`).

| Action | CLERK | MANAGER | No token |
|---|---|---|---|
| `POST /api/v1/auth/login` | anyone | anyone | allowed |
| Create, read, update, delete orders | yes | yes | 401 |
| Locked status change `POST /api/v1/orders/{id}/pay` | 403 | yes | 401 |
| `GET /api/me` | yes | yes | 401 |
| `GET /actuator/health` | anyone | anyone | allowed |
| springdoc UI and `/v3/api-docs/**` | anyone | anyone | allowed |
| Anything else | 403 | 403 | 401 |

- No token → **401** `UNAUTHENTICATED` (`valid token required` on API calls; `bad username or password` on failed login).
- CLERK on `pay` → **403** `FORBIDDEN` (`not allowed for your role`); the row does not change; the service is not called.
- MANAGER on `pay` (policy allows) → success; status becomes `PAID`. Actor is `Authentication.getName()`.

## Technologies

- Java 21
- Spring Boot 3.4.5
- Maven
- Spring Web + Validation
- Spring Security + OAuth2 Resource Server (JWT HS256)
- springdoc-openapi 2.8.8
- Spring Actuator
- Spring Data JPA / Hibernate
- Flyway
- PostgreSQL 16 (Docker Compose)
- JUnit 5, Mockito, AssertJ, spring-security-test
- JDBC / Spring `JdbcTemplate` (integration tests that probe constraints)

## Architecture

The project separates business rules, application logic, persistence, HTTP, security, and outbound integrations.

- **`domain`** — Order statuses, identifiers, business rules, and domain exceptions. Plain Java: no Spring, no `ResponseEntity`, no `jakarta.validation`, no security annotations.
- **`handler`** — REST controllers, request/response/error records, exception advice, and application service.
- **`security`** — Filter chain, JWT encoder/decoder, token service, entry point / access-denied handlers, dev users.
- **`persistence`** — JPA entity, Spring Data repository, and adapter. Jackson never sees the entity.
- **`outbound`** — Outbound ports/adapters (notifier). Downstream URL and timeout come from yaml.
- **`config`** — Application entry point and Spring bean configuration.

`OrderPolicy` is the earlier all-in-one rule object (labs 1–2). `Rule` / `TransitionRule` / `ForbiddenRule` / `RuleChain` is the later SOLID version used by `OrderService`.

### REST

- `POST /api/v1/auth/login` — BCrypt check → `accessToken`, `tokenType` (`Bearer`), `expiresIn` (`900`)
- `GET /api/me` — username + authorities from the token
- `POST /api/v1/orders` — create one order → `201` + `Location` (CLERK or MANAGER)
- `GET /api/v1/orders/{id}` — read by UUID → `200` (CLERK or MANAGER)
- `PUT /api/v1/orders/{id}` — update title while `CART` → `200` (CLERK or MANAGER); otherwise `409`
- `DELETE /api/v1/orders/{id}` — delete while `CART` → `204` (CLERK or MANAGER); otherwise `409`
- `POST /api/v1/orders/{id}/pay` — locked status change → `200` (MANAGER only)
- Failures use `{ code, message, correlationId }`
- OpenAPI UI: `/swagger-ui.html` (open)
- Health: `/actuator/health` (open; only `health` and `info` are exposed)
- Auth page: `http://localhost:8080/` (static files from `src/main/resources/static/`)

### JWT

- HS256, issuer `online-shop`, lifetime 15 minutes
- Claim `roles` carries role names **without** the `ROLE_` prefix
- Secret: `app.jwt.secret` in profile yaml (≥ 32 characters). Prod: `${CSS_JWT_SECRET}` (no default). No secret literal in `src/main/java`.

### Dev users (`@Profile("dev")`)

| Username | Role | Password |
|---|---|---|
| `clerk` | CLERK | `clerk-dev` |
| `manager` | MANAGER | `manager-dev` |

Passwords are encoded with `PasswordEncoderFactories.createDelegatingPasswordEncoder()` (BCrypt). No `{noop}`. No user table.

## Database and Persistence

PostgreSQL stores orders in the `shop_order` table. Start it with:

```bash
docker compose up -d
```

The schema is owned by Flyway:

`src/main/resources/db/migration/V1__create_shop_order.sql`

Hibernate uses `ddl-auto: validate`. `baseline-on-migrate: true` supports a DB that already has `shop_order` from earlier labs.

| Column | Purpose |
|---|---|
| `id` | UUID primary key |
| `business_key` | Unique business identifier for an order |
| `status` | Current order status (stored as text) |
| `title` | Order title |
| `created_at` | Creation timestamp generated by PostgreSQL |

Constraints: `PRIMARY KEY`, `UNIQUE(business_key)`, `NOT NULL`, `CHECK` on known statuses.

A duplicate business key becomes domain `DuplicateOrder` (`SQLState 23505`).

## Configuration / Profiles

- `application.yaml` — shared settings (JPA, Flyway, error leak off, actuator, JWT issuer). No DB password. No JWT secret.
- `application-dev.yaml` — localhost PostgreSQL `css`/`css`, downstream, JWT secret for the laptop
- `application-prod.yaml` — `CSS_DB_*`, downstream env vars, `CSS_JWT_SECRET`

`spring.profiles.default: dev`

## Testing

| Kind | Class | What it covers |
|---|---|---|
| Unit | `OrderPolicyTest` | Domain rules |
| Unit + Mockito | `OrderServiceTest` | Service with mocked ports |
| Unit | `TokenServiceTest` | Issue JWT then decode (subject, roles, exp) |
| Web slice | `OrderControllerWebTest` | Lab 4 201/400/404/409 with a token |
| Web slice | `SecurityWebTest` | 401, 403, MANAGER pay, `/api/me`, health open |
| Config | `DownstreamPropertiesTest` | `app.downstream` bound from `dev` yaml |
| Slice | `OrderJpaRepositorySliceTest` | `@DataJpaTest` |
| Integration | `OrderDatabaseTest` | Full context + PostgreSQL; duplicate keeps count `1` |

```bash
docker compose up -d
mvn -q verify
```

## Running the Application

```bash
docker compose up -d
mvn spring-boot:run
```

Open `http://localhost:8080/` for the auth page. Log in as `clerk` / `clerk-dev` or `manager` / `manager-dev`.

## Lab Scope

- Labs 1–3: domain, Spring, PostgreSQL/JPA/Flyway, transactions
- Lab 4: REST contract, springdoc, profiles, actuator, web slice tests
- Lab 5: Spring Security, JWT login, locked `pay`, auth page
- Lab 6: Docker image / real outbound call (not in this lab)
