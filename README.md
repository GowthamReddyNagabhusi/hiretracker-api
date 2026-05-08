# HireTrack API 🚀

A production-ready **Job Application Tracker REST API** built with Spring Boot, secured with JWT authentication, and deployed using a full CI/CD pipeline.

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17 + Spring Boot 4.0 |
| Database | MySQL 8.0 |
| Security | JWT (JSON Web Token) |
| Build Tool | Maven |
| Containerization | Docker + Docker Compose |
| CI/CD | Jenkins + GitHub Actions |

## 📌 Features

- User Registration & Login with JWT Authentication
- Add, Update, Delete Job Applications
- Status Workflow: `APPLIED → INTERVIEW → OFFER → REJECTED`
- Filter by Status or Company Name
- Dashboard Stats (total applied, offers, rejection rate)

## 🚀 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login & get JWT token | Public |
| GET | `/api/jobs` | Get all your applications | Required |
| POST | `/api/jobs` | Add new application | Required |
| PUT | `/api/jobs/{id}` | Update application | Required |
| DELETE | `/api/jobs/{id}` | Delete application | Required |
| GET | `/api/jobs/stats` | Get dashboard stats | Required |
| GET | `/api/jobs/filter` | Filter by status/company | Required |

## ⚙️ Setup & Run Locally

### Prerequisites
- Java 17+
- Docker Desktop
- Maven

### 1. Clone the repository
```bash
git clone https://github.com/GowthamReddyNagabhusi/hiretracker-api.git
cd hiretracker-api
```

### 2. Start MySQL with Docker
```bash
docker-compose up -d mysql
```

### 3. Run the application
```bash
mvnw spring-boot:run
```

### 4. API is live at