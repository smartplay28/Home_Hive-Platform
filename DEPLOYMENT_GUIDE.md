# HomeHive — Deployment & Operations Guide

Welcome to the final production guide for **HomeHive**. This document covers how to run the project locally, test the APIs, and deploy it to the cloud.

---

## 1. Running the Project Locally

### Prerequisites
Make sure you have the following installed:
- **Java 17 or higher** (We recommend Java 21 LTS)
- **Node.js** (v18+)
- **Redis** (running on `localhost:6379`)
- **Python 3.10+** (For the AI Service)

### Step 1: Start Redis
The backend requires Redis for caching and agent coordination.
- **Windows**: Run Redis using WSL (`sudo service redis-server start`) or use a Docker container:
  `docker run -d -p 6379:6379 redis`

### Step 2: Start the AI Service (FastAPI)
Open a terminal in the `AI-Service` folder:
```bash
cd AI-Service
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### Step 3: Start the Backend (Spring Boot)
Open a terminal in the `UC-Backend` folder. You must set the environment variables before running (or rely on your IDE's `.env` plugin):
```powershell
cd UC-Backend
$env:MONGO_URI="mongodb+srv://ak28akm_db_user:Mitsau%402005@cluster0.wqugpjl.mongodb.net/Urban_Crap?retryWrites=true&w=majority&appName=Cluster0"
$env:JWT_SECRET="HomeHive_Super_Secret_JWT_Key_AkshaProject_2026_AtLeast64CharsLongForSecurity!"
$env:REDIS_HOST="localhost"
mvn spring-boot:run
```

### Step 4: Start the Frontend (React / Vite)
Open a terminal in the `frontend` folder:
```bash
cd frontend
npm install
npm run dev
```

Your app is now running at `http://localhost:5173`!

---

## 2. How to Test the Project

### Unit & Integration Testing
We have written a comprehensive JUnit 5 and Mockito test suite for the backend.
To run the tests, use Java 17 or 21 (Java 26 currently has Mockito compatibility issues):
```bash
cd UC-Backend
mvn clean test
```

### Manual API Testing (Postman / cURL)
Because the platform is now fully secured with JWT, testing APIs requires an access token.

1. **Register a Customer**
   POST `http://localhost:8080/api/v1/auth/customer/register`
   ```json
   {
     "name": "Test User",
     "email": "test@example.com",
     "phone": 1234567890,
     "password": "password123"
   }
   ```
2. **Copy the `accessToken`** from the response.
3. **Make an Authenticated Request** (e.g., Get Profile)
   GET `http://localhost:8080/api/v1/customers/me`
   Headers: `Authorization: Bearer <your_access_token>`

---

## 3. Cloud Deployment Strategy

To move HomeHive from your laptop to the public internet, follow this 3-tier architecture deployment:

### Tier 1: The Database (MongoDB Atlas)
You have already done this! Your database is hosted on MongoDB Atlas. Ensure that under **Network Access** in the Atlas dashboard, you allow IP addresses from your backend hosting provider (or `0.0.0.0/0` for testing).

### Tier 2: The Backend & AI Service (Render or Railway)
We recommend **Render.com** or **Railway.app** because they natively support Docker and background workers.

1. **Spring Boot Backend**:
   - Create a `Dockerfile` in the `UC-Backend` folder.
   - Deploy as a "Web Service" on Render.
   - Add your Environment Variables (`MONGO_URI`, `JWT_SECRET`, etc.) in the Render dashboard.

2. **AI Service**:
   - Deploy the `AI-Service` folder as a separate "Web Service" using Python/Uvicorn.

3. **Redis**:
   - Provision a managed Redis instance (Render offers this built-in). Update the `REDIS_HOST` env var in your Spring Boot app.

### Tier 3: The Frontend (Vercel or Netlify)
1. Push your code to GitHub.
2. Log into **Vercel.com**.
3. Import the repository, select the `frontend` folder as the Root Directory.
4. Set the build command to `npm run build` and output directory to `dist`.
5. Add an Environment Variable: `VITE_API_URL=https://your-backend-url.onrender.com`.

### 4. Final Security Checklist Before Launch
- [ ] Ensure `.env` is never committed to GitHub (we fixed this in `.gitignore`).
- [ ] Ensure MongoDB Atlas Network Access is restricted to your Backend's IP address.
- [ ] Change the `JWT_SECRET` to a completely new, randomly generated 64-character string in production.
- [ ] Enable HTTPS/SSL on all services (Vercel and Render handle this automatically).

---
*Built with ❤️ by Aksha. Hardened for Production.*
