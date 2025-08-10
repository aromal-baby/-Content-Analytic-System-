# -Content-Analytic-System-
This is cross platform (social media) metrics analysis system like YouTube studio where  users can analyze their content's performance in the respective platform within a single application.


# Content Analytics System - Docker Implementation

## Project Overview
This project is a Content Analytics System built with Spring Boot and containerized using Docker. The system allows users to track and analyze metrics from various content platforms like YouTube and Instagram.

## Docker Implementation
The Docker implementation includes:
- Multi-stage build process for efficient image creation
- Docker Compose setup for orchestrating multiple services
- Non-root user configuration for improved security
- Volume management for persistent data
- Health checks for dependent services
- Environment variable configuration for easy deployment

## Prerequisites
- Docker and Docker Compose installed
- Git (to clone the repository)
- 4GB+ RAM allocated to Docker

## How to Run the Application

### Clone the Repository
```bash
git clone https://github.com/aromal-baby/-Content-Analytic-System-.git
cd content-analytics-system
```

### Start the Application
```bash
docker-compose up -d
```
This command will:
1. Build the Spring Boot application
2. Start MySQL and MongoDB containers
3. Initialize the database with necessary tables and stored procedures
4. Launch the application container

### Check Service Status
```bash
docker-compose ps
```

### View Logs
```bash
docker-compose logs -f app
```

### Access the Application
- API Endpoint: http://localhost:8080/api
- Postman Collection is available in the project's root directory

## Docker Components

### Dockerfile
- Uses multi-stage build to reduce final image size
- Separates dependency download from code compilation for better caching
- Implements security best practices (non-root user)
- Configures JVM for optimal container performance

### docker-compose.yml
- Orchestrates three services: MySQL, MongoDB, and the Spring Boot application
- Configures networking between services
- Sets up volumes for persistent data storage
- Defines environment variables for configuration
- Implements health checks to ensure dependent services are ready

### MySQL Initialization
- Automatically creates required database schemas
- Sets up stored procedures for complex operations
- Initializes with correct character set and collation

## System Architecture
```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│  Spring Boot    │◄────►│  MySQL DB       │      │  MongoDB        │
│  Application    │      │  (SQL Data)     │      │  (Metrics Data) │
│                 │      │                 │      │                 │
└────────┬────────┘      └─────────────────┘      └─────────────────┘
         │
         │
         ▼
┌─────────────────┐
│                 │
│  REST API       │
│  Endpoints      │
│                 │
└─────────────────┘
```

## Additional Information
- All services are configured to automatically restart unless stopped manually
- Data persists across container restarts through Docker volumes
- The application is configured to use a bridge network for inter-service communication

## Troubleshooting
- If the application fails to start, check logs with: `docker-compose logs app`
- For database issues: `docker-compose logs mysql` or `docker-compose logs mongodb`
- To rebuild the application: `docker-compose build app`
- To restart all services: `docker-compose restart`


## Screenshots :



## Video: 
[Watch how this works](https://youtu.be/jmX1ecudvDE?si=Yvbw_2X6TDYlQsn_)
