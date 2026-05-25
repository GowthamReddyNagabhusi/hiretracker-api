# HireTrack API 🚀

A production-ready **Job Application Tracker REST API** built with Spring Boot, secured with JWT authentication, and deployed using a full CI/CD pipeline.

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17 + Spring Boot 4.0 |
| **Database** | MySQL 8.0 |
| **Security** | JWT (JSON Web Token) |
| **Build Tool** | Maven |
| **Containerization** | Docker + Docker Compose |
| **CI/CD** | Jenkins + GitHub Actions |

## 📌 Features

- **User Authentication**: Secure user registration and login using JWT.
- **Job Applications Management**: Create, read, update, and delete (CRUD) job applications.
- **Status Workflow**: Track application states (`APPLIED → INTERVIEW → OFFER → REJECTED`).
- **Advanced Filtering**: Filter applications by status or company name.
- **Dashboard Stats**: View overall metrics including total applied, active offers, and rejection rates.

## 🚀 API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST   | `/api/auth/register` | Register a new user | No |
| POST   | `/api/auth/login`    | Login & receive JWT | No |

### Jobs Management
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET    | `/api/jobs`          | Retrieve all job applications | Yes |
| POST   | `/api/jobs`          | Create a new application | Yes |
| PUT    | `/api/jobs/{id}`     | Update an application | Yes |
| DELETE | `/api/jobs/{id}`     | Delete an application | Yes |
| GET    | `/api/jobs/stats`    | Retrieve dashboard statistics | Yes |
| GET    | `/api/jobs/filter`   | Filter applications by status/company | Yes |

## ⚙️ Setup & Run Locally

### Prerequisites
- [Java 17+](https://adoptium.net/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop)
- [Maven](https://maven.apache.org/)

### 1. Clone the repository
```bash
git clone https://github.com/GowthamReddyNagabhusi/hiretracker-api.git
cd hiretracker-api
```

### 2. Start MySQL with Docker
You can easily spin up the required MySQL database using Docker Compose:
```bash
docker-compose up -d mysql
```

### 3. Run the application
Start the Spring Boot application using the Maven wrapper:
```bash
./mvnw spring-boot:run
```

### 4. API is Live
The API will be accessible at:
```text
http://localhost:8080
```
Use tools like [Postman](https://www.postman.com/) or [cURL](https://curl.se/) to interact with the API endpoints.

## 🛡️ Continuous Integration
This project uses **GitHub Actions** for CI/CD. The pipeline automatically:
1. Provisions a test MySQL database.
2. Compiles and tests the code using Maven.
3. Builds the production Docker image.