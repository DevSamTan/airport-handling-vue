# AirOps Handling — Backend

Java 21 + Spring Boot 3.3 REST API for airport ground-handling shift management.

## Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.0 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA + Hibernate |
| DB (dev) | H2 in-memory |
| DB (prod) | PostgreSQL |
| Docs | springdoc-openapi 2.5 (Swagger UI) |
| Build | Maven |

---

## Quick start (dev)

```bash
cd backend
./mvnw spring-boot:run
```

The active profile defaults to `dev` (H2, DDL create-drop, SQL logging, seed data).

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Swagger UI |
| `http://localhost:8080/h2-console` | H2 console (JDBC URL: `jdbc:h2:mem:airopsdb`) |
| `http://localhost:8080/actuator/health` | Health check |

---

## Default seed users

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin123!` | ADMIN |
| `planner` | `Plan123!` | PLANNER |
| `hrviewer` | `Hr123!` | HR_VIEWER |

---

## Authentication

All protected endpoints require a `Bearer` token in the `Authorization` header.

**Login:**
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "Admin123!" }
```

**Response:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin",
  "role": "ADMIN"
}
```

**Refresh:**
```http
POST /api/auth/refresh
X-Refresh-Token: <refresh_token>
```

---

## Key endpoints

### Staff
| Method | Path | Role |
|---|---|---|
| GET | `/api/staff` | any auth |
| GET | `/api/staff/{id}` | any auth |
| POST | `/api/staff` | ADMIN |
| PUT | `/api/staff/{id}` | ADMIN |
| DELETE | `/api/staff/{id}` | ADMIN |

Query param `?department=RAMPA` filters the list.

### Shifts
| Method | Path | Notes |
|---|---|---|
| GET | `/api/shifts/weekly?weekStart=2025-05-05` | Full grid response |
| GET | `/api/shifts?weekStart=2025-05-05` | Raw list |
| GET | `/api/shifts/staff/{id}?year=2025&month=5` | Monthly history |
| POST | `/api/shifts` | ADMIN / PLANNER |
| PUT | `/api/shifts/{id}` | ADMIN / PLANNER |
| DELETE | `/api/shifts/{id}` | ADMIN / PLANNER |

Assigning a shift validates weekly contract hours (allows up to +8 h overtime).
A duplicate shift on the same staff+date returns `409 Conflict`.

### Shift Swaps
| Method | Path | Notes |
|---|---|---|
| GET | `/api/shift-swaps` | List all |
| GET | `/api/shift-swaps/pending` | Pending only |
| POST | `/api/shift-swaps` | Any auth |
| PATCH | `/api/shift-swaps/{id}/review?decision=APPROVED` | ADMIN / PLANNER |

### Sick Leaves
| Method | Path | Notes |
|---|---|---|
| GET | `/api/sick-leaves` | ADMIN / HR_VIEWER |
| POST | `/api/sick-leaves` | Any auth |
| PATCH | `/api/sick-leaves/{id}/status?status=CERT_RECEIVED` | ADMIN / HR_VIEWER |
| PATCH | `/api/sick-leaves/{id}/certificate?certificateUrl=...` | ADMIN / HR_VIEWER |

### Dashboard
```http
GET /api/dashboard
```
Returns today's KPIs, per-department coverage, and hourly staffing heatmap.

---

## Error format

All errors return a consistent JSON body:

```json
{
  "status": 409,
  "message": "Shift already assigned for Marco Rossi on 2025-05-06",
  "errors": null,
  "timestamp": "2025-05-06T09:30:00"
}
```

Validation errors include a field-level `errors` map:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "staffId": "must not be null",
    "date": "must not be null"
  },
  "timestamp": "2025-05-06T09:30:00"
}
```

---

## Production deployment

Set environment variables before running:

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://host:5432/airopsdb
DB_USERNAME=airops
DB_PASSWORD=secret
APP_JWT_SECRET=<base64-encoded-256-bit-key>
```

Generate a secure JWT secret:
```bash
openssl rand -base64 32
```
