# HomeHive 🐝
**Scalable, production-grade home services platform for Bangalore.**

> Upgraded from a basic CRUD prototype to a secure, real-time capable, cloud-deployable system.

---

## ✨ Features

- 🔐 **JWT Authentication** — BCrypt-hashed passwords, access + refresh tokens, role-based access (Customer / Agent / Admin)
- 🔔 **Real-Time WebSocket Notifications** — Agents receive instant push notifications for new orders; customers get live status updates (PENDING → AGENT_ASSIGNED → COMPLETED) via STOMP/SockJS
- 🏙️ **City-Focused Services** — Tailored service marketplace for Bangalore localities
- 📍 **Geospatial Agent Matching** — Finds nearest available agents using location-based distance calculation
- ⚡ **Async Order Processing** — Checkout returns immediately (202 Accepted), agent assignment runs in background thread pool
- 🔄 **Versioned REST API** — All endpoints under `/api/v1`, standardized `ApiResponse<T>` envelope
- 📊 **Observability Ready** — Spring Actuator health checks at `/actuator/health`
- 🛡️ **Security Hardened** — Spring Security, CORS config, structured error handling (no stack trace leaks)
- 🪵 **Structured Logging** — SLF4J with request tracing via log context

---

## 🏗️ Architecture

```
React (Vite + TailwindCSS)
    │  Axios + JWT Bearer token (src/lib/api.js)
    ▼
Spring Boot 3 — Layered Architecture
    controller/  →  service/  →  repository/  →  MongoDB Atlas
    security/    (JWT filter + BCrypt + Spring Security)
    exception/   (GlobalExceptionHandler — no bare catch blocks)
    config/      (AsyncConfig — @Async thread pool)
```

---

## 🛠️ Tech Stack

| Technology | Purpose |
|-----------|---------|
| **React 18 + Vite** | Frontend SPA |
| **TailwindCSS** | Styling |
| **Axios** | HTTP client with JWT interceptors |
| **@stomp/stompjs + SockJS** | WebSocket real-time notifications |
| **Spring Boot 3.3** | REST API backend |
| **Spring Security** | JWT auth + RBAC |
| **Spring WebSocket + STOMP** | Real-time pub/sub message broker |
| **MongoDB Atlas** | Document database with compound + 2dsphere indexes |
| **Redis 7.2** | Agent availability caching (30s TTL), order history caching (60s TTL) |
| **Java 17** | Application logic |
| **Lombok** | Boilerplate elimination |
| **jjwt 0.12.3** | JWT generation/validation |
| **Docker Compose** | Local dev infrastructure (Redis + Redis Insight) |
| **JUnit 5 + Mockito** | Backend unit tests |
| **Testcontainers (MongoDB)** | Backend integration tests with real DB |
| **Vitest + Testing Library** | Frontend component + API client tests |
| **Python 3 + FastAPI** | AI/ML Microservice for semantic search & dynamic pricing |
| **Sentence-Transformers** | NLP mapping of user queries to service categories |
| **scikit-learn + numpy** | Matrix operations and demand forecasting |

---

## 📁 Backend Package Structure

```
com.example.UC_Backend/
├── controller/          # REST controllers (/api/v1/**)
│   ├── AuthController        # /api/v1/auth/**  (public)
│   ├── OrderController       # /api/v1/orders/** (JWT protected)
│   ├── CustomerController    # /api/v1/customers/**
│   └── ServiceAgentController# /api/v1/agents/**
├── service/             # Business logic layer
│   ├── AuthService           # Register/login + JWT issuance
│   └── OrderService          # Order lifecycle + @Async assignment
├── security/            # Spring Security config
│   ├── JwtTokenProvider      # Token generation/validation
│   ├── JwtAuthFilter         # OncePerRequestFilter
│   └── SecurityConfig        # CORS + BCrypt + filter chain
├── common/
│   └── ApiResponse<T>        # Standardized response envelope
├── exception/           # Custom exceptions + global handler
├── config/
│   └── AsyncConfig           # @Async thread pool for order processing
├── dto/auth/            # Typed request/response records
├── Database/            # MongoDB repositories
├── Users/               # Domain models (Customer, ServiceAgent, Admin)
└── Extra/               # RangeChecker (geospatial distance)
```

---

## 🔑 API Reference

All responses follow:
```json
{ "success": true, "message": "...", "data": {...}, "timestamp": "..." }
```

### Auth (Public — no JWT required)
| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/v1/auth/customer/register` | `{ name, email, phone, password }` |
| POST | `/api/v1/auth/customer/login` | `{ email, password }` |
| POST | `/api/v1/auth/agent/register` | `{ name, email, password, skill[], range, location }` |
| POST | `/api/v1/auth/agent/login` | `{ email, password }` |
| POST | `/api/v1/auth/admin/login` | `{ email, password }` + Header: `X-Access-Code` |

### Orders (JWT Required)
| Method | Endpoint | Role |
|--------|----------|------|
| POST | `/api/v1/orders/checkout` | CUSTOMER |
| POST | `/api/v1/orders/history` | CUSTOMER, ADMIN |
| GET | `/api/v1/orders/cart/{customerId}` | CUSTOMER, ADMIN |
| POST | `/api/v1/orders/cart/add` | CUSTOMER |
| POST | `/api/v1/orders/cart/remove` | CUSTOMER |
| POST | `/api/v1/orders/accept` | AGENT, ADMIN |
| POST | `/api/v1/orders/reject` | AGENT, ADMIN |

---

## 🚀 How to Run Locally

You will need 4 terminal windows to run the complete HomeHive stack.

### Prerequisites
- Node.js 20+
- Java JDK 17+ & Maven 3.9+
- Python 3.10+
- Docker Desktop (for Redis)
- MongoDB Atlas account (or local MongoDB)

### 1. Start Infrastructure (Terminal 1)
Starts Redis (caching) and Redis Insight (GUI on port 8001).
```bash
docker-compose up -d
```

### 2. Start AI/ML Microservice (Terminal 2)
Starts the FastAPI service for semantic search and surge pricing on `http://localhost:8000`.
```bash
cd AI-Service
pip install -r requirements.txt
# The first run will download the HuggingFace model (~90MB)
uvicorn main:app --reload
```

### 3. Start Spring Boot Backend (Terminal 3)
Starts the core Java backend on `http://localhost:8080`.
```bash
cd UC-Backend
# Set environment variables (or let it use defaults in application.properties)
export JWT_SECRET=your_super_secret_jwt_key_that_is_at_least_32_chars_long
export MONGO_URI=your_mongodb_connection_string

mvn spring-boot:run
```
*Health Check:* `http://localhost:8080/actuator/health`

### 4. Start React Frontend (Terminal 4)
Starts the Vite frontend on `http://localhost:5173`.
```bash
cd frontend
npm install
npm run dev
```

---

## 🌍 Deployment Guide

HomeHive is architected to be easily deployed to modern cloud platforms.

### 1. Frontend (Vercel / Netlify)
1. Set the build command to `npm run build` and output directory to `dist`.
2. Add Environment Variable: `VITE_API_URL=https://your-backend-url.com/api/v1`

### 2. Backend (Render / Railway / AWS ECS)
1. **Option A (Docker):** Uncomment the `backend` service in `docker-compose.yml` to containerize the Spring Boot app.
2. **Option B (PaaS):** Deploy the `UC-Backend` folder as a Web Service. Set the Build Command to `mvn clean package -DskipTests` and Start Command to `java -jar target/*.jar`.
3. Provide `MONGO_URI`, `JWT_SECRET`, and `REDIS_HOST` environment variables.

### 3. AI Service (Render / Railway)
Deploy the `AI-Service` folder.
- Build Command: `pip install -r requirements.txt`
- Start Command: `uvicorn main:app --host 0.0.0.0 --port $PORT`

### 4. Database & Cache
- **MongoDB:** Use MongoDB Atlas (Free Tier is sufficient).
- **Redis:** Use Upstash (Serverless Redis) or Render's managed Redis.

## 🗺️ Upgrade Roadmap

- [x] Phase 1 — JWT Security + BCrypt + Layered Architecture
- [x] Phase 2 — WebSocket real-time order notifications (STOMP + SockJS)
- [x] Phase 3 — Redis caching + MongoDB compound indexes + 2dsphere geospatial matching
- [x] Phase 4 — Rating/review system (Welford avg) + agent availability scheduling
- [x] Phase 6 — JUnit 5 + Testcontainers integration tests + Vitest frontend tests
- [x] Phase 7 — AI/ML: Semantic search + dynamic pricing + demand forecasting
